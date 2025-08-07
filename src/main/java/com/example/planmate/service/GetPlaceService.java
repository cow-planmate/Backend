package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.PlaceResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.externalAPI.GoogleMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class GetPlaceService {
    private final GoogleMap googleMap;
    private final PlanAccessValidator planAccessValidator;


    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getTourPlace(travelCategoryName + " " +travelName + " " + "관광지"));
        return response;
    }
    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getLodgingPlace(travelCategoryName + " " +travelName + " " + "숙소"));
        return response;
    }
    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getRestaurantPlace(travelCategoryName + " " +travelName + " " + "식당"));
        return response;
    }

    public PlaceResponse getSearchPlace(int userId, int planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        response.addPlace(googleMap.getSearchPlace(query));
        return response;
    }


}
