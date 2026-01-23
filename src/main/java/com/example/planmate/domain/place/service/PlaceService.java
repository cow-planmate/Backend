package com.example.planmate.domain.place.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.externalAPI.GooglePlaceDetails;
import com.example.planmate.common.valueObject.LodgingPlaceVO;
import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.common.valueObject.RestaurantPlaceVO;
import com.example.planmate.common.valueObject.TourPlaceVO;
import com.example.planmate.domain.place.dto.PlaceResponse;
import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;
import com.example.planmate.domain.place.repository.PlaceSearchConditionRepository;
import com.example.planmate.domain.place.repository.PlaceSearchResultRepository;
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
    private final PlaceSearchConditionRepository placeSearchConditionRepository;
    private final PlaceSearchResultRepository placeSearchResultRepository;

    @FunctionalInterface
    private interface ThrowingBiFunction<T, U, R> {
        R apply(T t, U u) throws IOException;
    }

    /**
     * Generic helper to reduce duplication across place retrieval methods.
     *
     * @param userId user id used to load preferred themes
     * @param planId plan id to validate access and get travel info
     * @param preferredThemeCategoryId filter id for PreferredThemeCategory (0=tour,1=lodging,2=restaurant)
     * @param googleMapFn function that calls the appropriate googleMap method and returns Pair<List<T>, List<String>>
     * @param <T> concrete VO type that extends PlaceVO
     * @return PlaceResponse with places and next page token
     * @throws IOException if external calls fail
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Transactional
    private PlaceResponse getPlaceForUserAndPlan(int userId,
                                                 int planId,
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

        // 2. Prepare a list of tasks. If no themes selected, we do one "default" search.
        // Otherwise, we do one search per theme to ensure per-theme caching.
        List<PreferredTheme> tasksToProcess = new ArrayList<>(filteredThemes);
        if (tasksToProcess.isEmpty()) {
            tasksToProcess.add(null); // Represents the "no theme selected" case
        }

        Map<String, PlaceVO> aggregatedPlaces = new LinkedHashMap<>();
        List<String> aggregatedTokens = new ArrayList<>();

        for (PreferredTheme theme : tasksToProcess) {
            Integer targetThemeId = (theme != null) ? theme.getPreferredThemeId() : null;
            String themeName = (theme != null) ? theme.getPreferredThemeName() : "";
            List<String> searchThemes = (theme != null) ? List.of(themeName) : new ArrayList<>();

            // -------------------------------------------------------------
            // Try to load from Cache first (per single theme)
            // -------------------------------------------------------------
            String cacheKey = travelId + ":" + preferredThemeCategoryId + ":" + (targetThemeId != null ? targetThemeId : "");
            var existingConditionOpt = placeSearchConditionRepository.findByCacheKey(cacheKey);

            if (existingConditionOpt.isPresent()) {
                PlaceSearchCondition existingCondition = existingConditionOpt.get();
                if (existingCondition.getExpiredAt().isAfter(LocalDateTime.now())) {
                    List<PlaceSearchResult> cachedResults = placeSearchResultRepository.findAllByCondition(existingCondition);
                    if (!cachedResults.isEmpty()) {
                        for (PlaceSearchResult r : cachedResults) {
                            if (!aggregatedPlaces.containsKey(r.getPlaceId())) {
                                PlaceVO vo;
                                if (preferredThemeCategoryId == 0) {
                                    vo = new TourPlaceVO(r.getPlaceId(), 0, "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId(), r.getPlaceName(), r.getPlaceAddress(), r.getPlaceRating().floatValue(), r.getPhotoUrl(), r.getXLocation() != null ? r.getXLocation() : 0, r.getYLocation() != null ? r.getYLocation() : 0, r.getIconUrl());
                                } else if (preferredThemeCategoryId == 1) {
                                    vo = new LodgingPlaceVO(r.getPlaceId(), 1, "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId(), r.getPlaceName(), r.getPlaceAddress(), r.getPlaceRating().floatValue(), r.getPhotoUrl(), r.getXLocation() != null ? r.getXLocation() : 0, r.getYLocation() != null ? r.getYLocation() : 0, r.getIconUrl());
                                } else {
                                    vo = new RestaurantPlaceVO(r.getPlaceId(), 2, "https://www.google.com/maps/place/?q=place_id:" + r.getPlaceId(), r.getPlaceName(), r.getPlaceAddress(), r.getPlaceRating().floatValue(), r.getPhotoUrl(), r.getXLocation() != null ? r.getXLocation() : 0, r.getYLocation() != null ? r.getYLocation() : 0, r.getIconUrl());
                                }
                                aggregatedPlaces.put(vo.getPlaceId(), vo);
                            }
                        }
                        continue; // Successfully loaded from cache for this theme
                    }
                }
            }

            // -------------------------------------------------------------
            // Cache Miss: Fetch from Google (for this specific theme)
            // -------------------------------------------------------------
            Pair<Double, Double> coords = googleMap.getCoordinates(travelName);
            Double lat = (coords != null) ? coords.getFirst() : null;
            Double lng = (coords != null) ? coords.getSecond() : null;

            Pair rawPair;
            boolean includeBaseline = (theme == null); // 테마가 없을 때만 기본값(Baseline) 포함

            if (preferredThemeCategoryId == 0) {
                rawPair = googleMap.getTourPlace(travelName, searchThemes, lat, lng, includeBaseline);
            } else if (preferredThemeCategoryId == 1) {
                rawPair = googleMap.getLodgingPlace(travelName, searchThemes, lat, lng, includeBaseline);
            } else {
                rawPair = googleMap.getRestaurantPlace(travelName, searchThemes, lat, lng, includeBaseline);
            }
            
            Pair<List<? extends PlaceVO>, List<String>> pair = (Pair) rawPair;
            List<PlaceVO> detailed = (List<PlaceVO>) pair.getFirst();
            aggregatedTokens.addAll(pair.getSecond());

            // -------------------------------------------------------------
            // Save results to cache (per theme)
            // -------------------------------------------------------------
            // Use native upsert to handle concurrent inserts of the same cacheKey without unique constraint violations
            placeSearchConditionRepository.upsertCondition(
                    travelId,
                    preferredThemeCategoryId,
                    targetThemeId,
                    cacheKey,
                    LocalDateTime.now().plusDays(360)
            );

            PlaceSearchCondition condition = placeSearchConditionRepository.findByCacheKey(cacheKey)
                    .orElseThrow(() -> new RuntimeException("Condition should exist after upsert"));

            // Clear old results using the optimized delete method
            placeSearchResultRepository.deleteAllByCondition(condition);

            List<PlaceSearchResult> resultsToSave = new ArrayList<>();
            for (PlaceVO vo : detailed) {
                if (!aggregatedPlaces.containsKey(vo.getPlaceId())) {
                    aggregatedPlaces.put(vo.getPlaceId(), vo);
                }
                resultsToSave.add(PlaceSearchResult.builder()
                        .condition(condition)
                        .placeId(vo.getPlaceId())
                        .placeName(vo.getName())
                        .placeAddress(vo.getFormatted_address())
                        .placeRating(BigDecimal.valueOf(vo.getRating()))
                        .photoUrl(vo.getPhotoUrl())
                        .iconUrl(vo.getIconUrl())
                        .placeLink(vo.getUrl())
                        .xLocation(vo.getXLocation())
                        .yLocation(vo.getYLocation())
                        .build());
            }
            if (!resultsToSave.isEmpty()) {
                placeSearchResultRepository.saveAll(resultsToSave);
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

    @Transactional
    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 0);
    }

    @Transactional
    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 1);
    }

    @Transactional
    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 2);
    }

    @Transactional
    public PlaceResponse getSearchPlace(int userId, int planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);

        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();

        // Get coordinates for the plan's destination to provide location-based search
        Pair<Double, Double> coords = googleMap.getCoordinates(travelName);
        Double lat = (coords != null) ? coords.getFirst() : null;
        Double lng = (coords != null) ? coords.getSecond() : null;

        Pair<List<PlaceVO>, List<String>> pair = googleMap.getSearchPlace(query, travelName, lat, lng);

        List<PlaceVO> places = pair.getFirst();

        // Trigger background image fetch with cache check
        fetchImagesWithCacheCheck(places);

        response.addPlace(places);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getTourPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        
        fetchImagesWithCacheCheck(pair.getFirst());
        
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getLodgingPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        
        fetchImagesWithCacheCheck(pair.getFirst());
        
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getRestaurantPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap.getRestaurantPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        
        fetchImagesWithCacheCheck(pair.getFirst());
        
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getSearchPlace(String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<PlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        
        fetchImagesWithCacheCheck(pair.getFirst());
        
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    

    @Transactional
    public PlaceResponse getNextPlace(List<String> nextPageToken) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<PlaceVO>, List<String>> pair = googleMap.getNextPagePlace(nextPageToken);
        
        fetchImagesWithCacheCheck(pair.getFirst());
        
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    /**
     * Helper to check DB for existing images before calling Google,
     * and sync newly fetched images back to DB.
     */
    private void fetchImagesWithCacheCheck(List<? extends PlaceVO> places) {
        if (places == null || places.isEmpty()) return;

        // 1. Try to fill from DB first to avoid unnecessary Google API calls
        for (PlaceVO vo : places) {
            if (vo.getPhotoUrl() == null || vo.getPhotoUrl().isBlank()) {
                placeSearchResultRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(vo.getPlaceId())
                        .ifPresent(existing -> vo.setPhotoUrl(existing.getPhotoUrl()));
            }
        }

        // 2. Fetch missing from Google and update all matching records in DB when done
        googlePlaceDetails.fetchMissingImagesInBackground(places, (placeId, photoUrl) -> {
            try {
                placeSearchResultRepository.updatePhotoUrlByPlaceId(placeId, photoUrl);
            } catch (Exception e) {
                // Ignore errors in background sync to not disrupt main flow
            }
        });
    }
}
