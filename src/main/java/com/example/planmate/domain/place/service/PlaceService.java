package com.example.planmate.domain.place.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Plan cachePlan = redisService.findPlanByPlanId(planId);
        String travelCategoryName;
        if(cachePlan!=null){
            travelCategoryName = cachePlan.getTravel().getTravelCategory().getTravelCategoryName();
        } else {
            travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        }
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 0);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " " + travelName, preferredThemeNames);
        List<TourPlaceVO> tourPlaceVOs = (List<TourPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(tourPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Plan cachePlan = redisService.findPlanByPlanId(planId);
        String travelCategoryName;
        if(cachePlan!=null){
            travelCategoryName = cachePlan.getTravel().getTravelCategory().getTravelCategoryName();
        } else {
            travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        }
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 1);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " " + travelName, preferredThemeNames);
        List<LodgingPlaceVO> lodgingPlaceVOs = (List<LodgingPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(lodgingPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Plan cachePlan = redisService.findPlanByPlanId(planId);
        String travelCategoryName;
        if(cachePlan!=null){
            travelCategoryName = cachePlan.getTravel().getTravelCategory().getTravelCategoryName();
        } else {
            travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        }
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 2);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " " + plan.getTravel().getTravelName();
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap.getRestaurantPlace(travelCategoryName + " " + travelName, preferredThemeNames);
        List<RestaurantPlaceVO> restaurantPlaceVOs = (List<RestaurantPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(restaurantPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
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
