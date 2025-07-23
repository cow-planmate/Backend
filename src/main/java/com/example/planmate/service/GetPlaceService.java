package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.PlaceResponse;
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
        String travelName = planAccessValidator.validateUserHasAccessToPlan(userId, planId).getTravel().getTravelName();
        response.addPlace(googleMap.getTourPlace(travelName + " " + "관광지"));
        return response;
    }
    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        String travelName = planAccessValidator.validateUserHasAccessToPlan(userId, planId).getTravel().getTravelName();
        response.addPlace(googleMap.getLodgingPlace(travelName + " " + "숙소"));
        return response;
    }
    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        String travelName = planAccessValidator.validateUserHasAccessToPlan(userId, planId).getTravel().getTravelName();
        response.addPlace(googleMap.getRestaurantPlace(travelName + " " + "식당"));
        return response;
    }

}
