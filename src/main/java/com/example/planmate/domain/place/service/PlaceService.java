package com.example.planmate.domain.place.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.externalAPI.GooglePlaceDetails;
import com.example.planmate.common.valueObject.LodgingPlaceVO;
import com.example.planmate.common.valueObject.RestaurantPlaceVO;
import com.example.planmate.common.valueObject.SearchPlaceVO;
import com.example.planmate.common.valueObject.TourPlaceVO;
import com.example.planmate.domain.place.dto.PlaceResponse;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.user.entity.PreferredTheme;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.service.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final GoogleMap googleMap;
    private final GooglePlaceDetails googlePlaceDetails;
    private final RedisService redisService;

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
     * @param <T> concrete VO type that extends SearchPlaceVO
     * @return PlaceResponse with places and next page token
     * @throws IOException if external calls fail
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private PlaceResponse getPlaceForUserAndPlan(int userId,
                                                 int planId,
                                                 int preferredThemeCategoryId,
                                                 ThrowingBiFunction<String, List<String>, Pair> googleMapFn) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Plan cachePlan = redisService.findPlanByPlanId(planId);

        String travelCategoryName = (cachePlan != null)
            ? cachePlan.getTravel().getTravelCategory().getTravelCategoryName()
            : plan.getTravel().getTravelCategory().getTravelCategoryName();

        // preserve original behavior where travelName included category (keeps semantics identical)
        String travelName = (cachePlan != null)
            ? travelCategoryName + " " + cachePlan.getTravel().getTravelName()
            : travelCategoryName + " " + plan.getTravel().getTravelName();

        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != preferredThemeCategoryId);

        List<String> preferredThemeNames = preferredThemes.stream()
            .map(PreferredTheme::getPreferredThemeName)
            .collect(Collectors.toList());

        Pair rawPair = googleMapFn.apply(travelCategoryName + " " + travelName, preferredThemeNames);
        Pair<List<? extends SearchPlaceVO>, List<String>> pair = (Pair) rawPair;
        List<?> detailedRaw = (List<?>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        List<SearchPlaceVO> detailed = (List<SearchPlaceVO>) detailedRaw;
        response.addPlace(detailed);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 0, (search, themes) -> googleMap.getTourPlace(search, themes));
    }

    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 1, (search, themes) -> googleMap.getLodgingPlace(search, themes));
    }

    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        return getPlaceForUserAndPlan(userId, planId, 2, (search, themes) -> googleMap.getRestaurantPlace(search, themes));
    }

    public PlaceResponse getSearchPlace(int userId, int planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        List<SearchPlaceVO> searchPlaceVOs = (List<SearchPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(searchPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getTourPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getLodgingPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getRestaurantPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap.getRestaurantPlace(travelCategoryName + " " + travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getSearchPlace(String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    

    public PlaceResponse getNextPlace(List<String> nextPageToken) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getNextPagePlace(nextPageToken);
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }
}
