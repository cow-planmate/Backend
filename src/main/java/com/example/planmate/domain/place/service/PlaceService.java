package com.example.planmate.domain.place.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.externalAPI.GooglePlaceDetails;
import com.example.planmate.common.valueObject.LodgingPlaceVO;
import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.common.valueObject.RestaurantPlaceVO;
import com.example.planmate.common.valueObject.TourPlaceVO;
import com.example.planmate.domain.place.dto.NextPageTokenDTO;
import com.example.planmate.domain.place.dto.NextPlaceRequest;
import com.example.planmate.domain.place.dto.PlaceResponse;
import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.user.entity.PreferredTheme;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final GoogleMap googleMap;
    private final GooglePlaceDetails googlePlaceDetails;
    private final PlaceTransactionService placeTransactionService;

    @FunctionalInterface
    private interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws IOException;
    }

    /**
     * Generic helper to reduce duplication across place retrieval methods.
     *
     * @param userId                   user id used to load preferred themes
     * @param planId                   plan id to validate access and get travel
     *                                 info
     * @param preferredThemeCategoryId filter id for PreferredThemeCategory
     *                                 (0=tour,1=lodging,2=restaurant)
     * @param googleMapFn              function that calls the appropriate googleMap
     *                                 method and returns Pair<List<T>,
     *                                 List<String>>
     * @param <T>                      concrete VO type that extends PlaceVO
     * @return PlaceResponse with places and next page token
     * @throws IOException if external calls fail
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private PlaceResponse getPlaceForUserAndPlan(UUID userId,
            UUID planId,
            int preferredThemeCategoryId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);

        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();
        int travelId = plan.getTravel().getTravelId();

        // 1. Get user's preferred themes for this category
        List<PreferredTheme> userThemes = userRepository.findById(userId).get().getPreferredThemes();
        List<PreferredTheme> filteredThemes = userThemes.stream()
                .filter(pt -> pt.getPreferredThemeCategory().getPreferredThemeCategoryId() == preferredThemeCategoryId)
                .collect(Collectors.toList());

        // 2. Prepare a list of tasks. If no themes selected, we do one "default"
        // search.
        // Otherwise, we do one search per theme to ensure per-theme caching.
        List<PreferredTheme> tasksToProcess = new ArrayList<>(filteredThemes);
        if (tasksToProcess.isEmpty()) {
            tasksToProcess.add(null); // Represents the "no theme selected" case
        }

        Map<String, PlaceVO> aggregatedPlaces = new LinkedHashMap<>();
        List<NextPageTokenDTO> aggregatedTokens = new ArrayList<>();

        for (int i = 0; i < tasksToProcess.size(); i++) {
            PreferredTheme theme = tasksToProcess.get(i);
            Integer targetThemeId = (theme != null) ? theme.getPreferredThemeId() : null;
            String themeName = (theme != null) ? theme.getPreferredThemeName() : "";
            List<String> searchThemes = (theme != null) ? List.of(themeName) : new ArrayList<>();

            // -------------------------------------------------------------
            // Try to load from Cache first (per single theme)
            // -------------------------------------------------------------
            String cacheKey = travelId + ":" + preferredThemeCategoryId + ":"
                    + (targetThemeId != null ? targetThemeId : "");
            var existingConditionOpt = placeTransactionService.getValidCondition(cacheKey);

            if (existingConditionOpt.isPresent()) {
                PlaceSearchCondition existingCondition = existingConditionOpt.get();
                List<PlaceSearchResult> first20 = placeTransactionService.getCachedResults(existingCondition, 1, 20);
                if (!first20.isEmpty()) {
                    for (PlaceSearchResult r : first20) {
                        double rating = (r.getPlaceRating() != null) ? r.getPlaceRating().doubleValue() : 0.0;
                        // 4.0 이상만 포함
                        if (rating < 4.0)
                            continue;

                        if (!aggregatedPlaces.containsKey(r.getPlaceId())) {
                            PlaceVO vo;
                            double x = (r.getXLocation() != null) ? r.getXLocation() : 0.0;
                            double y = (r.getYLocation() != null) ? r.getYLocation() : 0.0;
                            String placeLink = "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId();

                            if (preferredThemeCategoryId == 0) {
                                vo = new TourPlaceVO(r.getPlaceId(), preferredThemeCategoryId, placeLink,
                                        r.getPlaceName(), r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y,
                                        r.getIconUrl());
                            } else if (preferredThemeCategoryId == 1) {
                                vo = new LodgingPlaceVO(r.getPlaceId(), preferredThemeCategoryId, placeLink,
                                        r.getPlaceName(), r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y,
                                        r.getIconUrl());
                            } else {
                                vo = new RestaurantPlaceVO(r.getPlaceId(), preferredThemeCategoryId, placeLink,
                                        r.getPlaceName(), r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y,
                                        r.getIconUrl());
                            }
                            aggregatedPlaces.put(vo.getPlaceId(), vo);
                        }
                    }

                    // Check if there are more than 20 results or if we have next page tokens
                    int maxOrder = placeTransactionService.getMaxSortOrder(existingCondition);
                    if (maxOrder > 20) {
                        aggregatedTokens.add(NextPageTokenDTO.builder()
                                .token(cacheKey)
                                .page(2)
                                .build());
                    }

                    // 합계가 부족하고 마지막 테마였다면, 기본값 조사를 리스트에 추가
                    if (i == tasksToProcess.size() - 1 && aggregatedPlaces.size() < 20 && theme != null) {
                        tasksToProcess.add(null);
                    }
                    continue; // Successfully loaded from cache for this theme
                }
            }

            // -------------------------------------------------------------
            // Cache Miss: Fetch from Google (for this specific theme)
            // -------------------------------------------------------------
            Pair<Double, Double> coords = googleMap.getCoordinates(travelName);
            Double lat = (coords != null) ? coords.getFirst() : null;
            Double lng = (coords != null) ? coords.getSecond() : null;

            Pair rawPair;
            // 루프 안에서는 개별 테마에만 집중하고, 기본값(Baseline)은 루프가 끝난 뒤 합계가 부족할 때만 처리합니다.
            boolean includeBaseline = (theme == null);

            if (preferredThemeCategoryId == 0) {
                rawPair = googleMap.getTourPlace(travelName, searchThemes, lat, lng, includeBaseline);
            } else if (preferredThemeCategoryId == 1) {
                rawPair = googleMap.getLodgingPlace(travelName, searchThemes, lat, lng, includeBaseline);
            } else {
                rawPair = googleMap.getRestaurantPlace(travelName, searchThemes, lat, lng, includeBaseline);
            }

            Pair<List<? extends PlaceVO>, List<String>> pair = (Pair) rawPair;
            List<PlaceVO> detailed = (List<PlaceVO>) pair.getFirst();
            List<String> nextTokens = pair.getSecond();

            // Use native upsert to handle concurrent inserts of the same cacheKey without
            // unique constraint violations
            PlaceSearchCondition condition = placeTransactionService.saveSearchCondition(
                    travelId,
                    preferredThemeCategoryId,
                    targetThemeId,
                    cacheKey);

            // Clear old results using the optimized delete method
            placeTransactionService.clearOldResults(condition);

            int currentSortOrder = 1;
            placeTransactionService.saveSearchResults(condition, detailed, currentSortOrder);
            currentSortOrder += detailed.size();

            // If more initial tokens exist, start pre-fetching and provide a cached marker
            // for the frontend
            if (!nextTokens.isEmpty()) {
                preFetchRemainingPages(cacheKey, nextTokens, currentSortOrder, preferredThemeCategoryId);
                aggregatedTokens.add(NextPageTokenDTO.builder()
                        .token(cacheKey)
                        .page(2)
                        .build());
            }

            for (PlaceVO vo : detailed) {
                float rating = vo.getRating();
                // 4.0 이상만 포함하여 중복 제거 후 추가
                if (rating >= 4.0 && !aggregatedPlaces.containsKey(vo.getPlaceId())) {
                    aggregatedPlaces.put(vo.getPlaceId(), vo);
                }
            }

            // 합계가 부족하고 마지막 테마였다면, 기본값 조사를 리스트에 추가
            if (i == tasksToProcess.size() - 1 && aggregatedPlaces.size() < 20 && theme != null) {
                tasksToProcess.add(null);
            }
        }

        List<PlaceVO> finalPlaces = new ArrayList<>(aggregatedPlaces.values());
        fetchImagesWithCacheCheck(finalPlaces);

        response.addPlace(finalPlaces);
        if (!aggregatedTokens.isEmpty()) {
            response.addNextPageToken(aggregatedTokens);
        }
        return response;
    }

    public PlaceResponse getTourPlace(UUID userId, UUID planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 0);
    }

    public PlaceResponse getLodgingPlace(UUID userId, UUID planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 1);
    }

    public PlaceResponse getRestaurantPlace(UUID userId, UUID planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 2);
    }

    public PlaceResponse getSearchPlace(UUID userId, UUID planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);

        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();
        int travelId = plan.getTravel().getTravelId();

        // Use cache logic for general search too
        String cacheKey = travelId + ":4:" + query;
        var existingConditionOpt = placeTransactionService.getValidCondition(cacheKey);

        if (existingConditionOpt.isPresent()) {
            PlaceSearchCondition existingCondition = existingConditionOpt.get();
            List<PlaceSearchResult> first20 = placeTransactionService.getCachedResults(existingCondition, 1, 20);
            if (!first20.isEmpty()) {
                // Return first 20 from cache
                List<PlaceVO> places = first20.stream()
                        .filter(r -> {
                            double rating = (r.getPlaceRating() != null) ? r.getPlaceRating().doubleValue() : 0.0;
                            return rating >= 4.0;
                        })
                        .map(r -> {
                            double x = (r.getXLocation() != null) ? r.getXLocation() : 0.0;
                            double y = (r.getYLocation() != null) ? r.getYLocation() : 0.0;
                            float rating = (r.getPlaceRating() != null) ? r.getPlaceRating().floatValue() : 0.0f;
                            String placeLink = (r.getPlaceLink() != null) ? r.getPlaceLink()
                                    : "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId();
                            return new PlaceVO(r.getPlaceId(), 4, placeLink, r.getPlaceName(), r.getPlaceAddress(),
                                    rating, r.getPhotoUrl(), x, y, r.getIconUrl());
                        })
                        .collect(Collectors.toList());
                fetchImagesWithCacheCheck(places);
                response.addPlace(places);

                int maxOrder = placeTransactionService.getMaxSortOrder(existingCondition);
                if (maxOrder > 20) {
                    response.addNextPageToken(List.of(NextPageTokenDTO.builder()
                            .token(cacheKey)
                            .page(2)
                            .build()));
                }
                return response;
            }
        }

        // Cache Miss: Fetch from Google
        Pair<Double, Double> coords = googleMap.getCoordinates(travelName);
        Double lat = (coords != null) ? coords.getFirst() : null;
        Double lng = (coords != null) ? coords.getSecond() : null;

        Pair<List<PlaceVO>, List<String>> initialPair = googleMap.getSearchPlace(query, travelName, lat, lng);
        List<PlaceVO> detailed = initialPair.getFirst();
        List<String> nextTokens = initialPair.getSecond();

        // Save to Cache
        PlaceSearchCondition condition = placeTransactionService.saveSearchCondition(travelId, 4, null, cacheKey);
        placeTransactionService.clearOldResults(condition);

        int currentSortOrder = 1;
        placeTransactionService.saveSearchResults(condition, detailed, currentSortOrder);
        currentSortOrder += detailed.size();

        List<PlaceVO> filteredDetailed = detailed.stream()
                .filter(v -> v.getRating() >= 4.0)
                .collect(Collectors.toList());
        fetchImagesWithCacheCheck(filteredDetailed);
        response.addPlace(filteredDetailed);

        if (!nextTokens.isEmpty()) {
            preFetchRemainingPages(cacheKey, nextTokens, currentSortOrder, 4);
            response.addNextPageToken(List.of(NextPageTokenDTO.builder()
                    .token(cacheKey)
                    .page(2)
                    .build()));
        }

        return response;
    }

    private void preFetchRemainingPages(String cacheKey, List<String> nextTokens, int startSortOrder, int categoryId) {
        CompletableFuture.runAsync(() -> {
            final int[] currentOrderArr = { startSortOrder };
            List<String> tokens = nextTokens;

            while (!tokens.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                try {
                    Pair<List<PlaceVO>, List<String>> nextPair = googleMap.getNextPagePlace(tokens, categoryId);
                    if (nextPair.getFirst().isEmpty())
                        break;

                    List<PlaceVO> detailed = nextPair.getFirst();
                    tokens = nextPair.getSecond();

                    placeTransactionService.getValidCondition(cacheKey).ifPresent(condition -> {

                        placeTransactionService.saveSearchResults(condition, detailed, currentOrderArr[0]);
                        currentOrderArr[0] += detailed.size();

                        List<PlaceVO> filteredDetailed = detailed.stream()
                                .filter(v -> v.getRating() >= 4.0)
                                .collect(Collectors.toList());
                        fetchImagesWithCacheCheck(filteredDetailed);
                    });

                } catch (IOException e) {
                    break;
                }
            }
        });
    }

    public PlaceResponse getNextPlace(NextPlaceRequest request) throws IOException {
        PlaceResponse response = new PlaceResponse();
        List<NextPageTokenDTO> incomingTokens = request.getTokens();

        List<PlaceVO> allPlaces = new ArrayList<>();
        List<NextPageTokenDTO> allNextTokens = new ArrayList<>();

        for (NextPageTokenDTO incoming : incomingTokens) {
            String cacheKey = incoming.getToken();
            if (cacheKey == null || cacheKey.isEmpty() || incoming.getPage() == null)
                continue;

            Integer targetPage = incoming.getPage();
            int startOrder = (targetPage - 1) * 20 + 1;
            int endOrder = targetPage * 20;

            Optional<PlaceSearchCondition> conditionOpt = placeTransactionService.getValidCondition(cacheKey);

            if (conditionOpt.isPresent()) {
                PlaceSearchCondition condition = conditionOpt.get();
                int categoryId = condition.getPlaceCategoryId();
                List<PlaceSearchResult> pageResults = placeTransactionService.getCachedResults(condition, startOrder,
                        endOrder);

                if (pageResults.isEmpty()) {
                    try {
                        Thread.sleep(500);
                        pageResults = placeTransactionService.getCachedResults(condition, startOrder, endOrder);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (!pageResults.isEmpty()) {
                    for (PlaceSearchResult r : pageResults) {
                        double rating = (r.getPlaceRating() != null) ? r.getPlaceRating().doubleValue() : 0.0;
                        if (rating < 4.0)
                            continue;

                        PlaceVO vo;
                        double x = (r.getXLocation() != null) ? r.getXLocation() : 0.0;
                        double y = (r.getYLocation() != null) ? r.getYLocation() : 0.0;
                        String placeLink = "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId();

                        if (categoryId == 0) {
                            vo = new TourPlaceVO(r.getPlaceId(), categoryId, placeLink, r.getPlaceName(),
                                    r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y, r.getIconUrl());
                        } else if (categoryId == 1) {
                            vo = new LodgingPlaceVO(r.getPlaceId(), categoryId, placeLink, r.getPlaceName(),
                                    r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y, r.getIconUrl());
                        } else if (categoryId == 2) {
                            vo = new RestaurantPlaceVO(r.getPlaceId(), categoryId, placeLink, r.getPlaceName(),
                                    r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y, r.getIconUrl());
                        } else {
                            vo = new PlaceVO(r.getPlaceId(), categoryId, placeLink, r.getPlaceName(),
                                    r.getPlaceAddress(), (float) rating, r.getPhotoUrl(), x, y, r.getIconUrl());
                        }
                        allPlaces.add(vo);
                    }

                    int maxOrder = placeTransactionService.getMaxSortOrder(condition);
                    if (maxOrder > endOrder) {
                        allNextTokens.add(NextPageTokenDTO.builder()
                                .token(cacheKey)
                                .page(targetPage + 1)
                                .build());
                    }
                }
            }
        }

        fetchImagesWithCacheCheck(allPlaces);

        Map<String, PlaceVO> deduplicatedMap = new LinkedHashMap<>();
        for (PlaceVO vo : allPlaces) {
            deduplicatedMap.putIfAbsent(vo.getPlaceId(), vo);
        }

        response.addPlace(new ArrayList<>(deduplicatedMap.values()));
        response.addNextPageToken(allNextTokens);
        return response;
    }

    // --- Legacy / Compatibility methods for endpoints without userId/planId ---

    public PlaceResponse getTourPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " " + travelName,
                new ArrayList<>());
        fetchImagesWithCacheCheck(pair.getFirst());
        response.addPlace(pair.getFirst());
        return response;
    }

    public PlaceResponse getLodgingPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " " + travelName,
                new ArrayList<>());
        fetchImagesWithCacheCheck(pair.getFirst());
        response.addPlace(pair.getFirst());
        return response;
    }

    public PlaceResponse getRestaurantPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap
                .getRestaurantPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        fetchImagesWithCacheCheck(pair.getFirst());
        response.addPlace(pair.getFirst());
        return response;
    }

    public PlaceResponse getSearchPlace(String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<PlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        fetchImagesWithCacheCheck(pair.getFirst());
        response.addPlace(pair.getFirst());
        return response;
    }

    private void fetchImagesWithCacheCheck(List<? extends PlaceVO> places) {
        placeTransactionService.updatePhotoUrlIfMissing(places);
        googlePlaceDetails.fetchMissingImagesInBackground(places, null);
    }
}
