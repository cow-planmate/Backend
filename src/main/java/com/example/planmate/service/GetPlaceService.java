package com.example.planmate.service;

import com.example.planmate.dto.PlaceResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.externalAPI.GoogleMap;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetPlaceService {
    private PlanRepository planRepository;
    private GoogleMap googleMap;

    public PlaceResponse getLodgingPlace(int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        String travelName = getTravelName(planId);
        response.addPlace(googleMap.getLodgingPlace(travelName + " " + "숙소"));
        return response;
    }
    public String getTravelName(int planId) throws IOException {
        Optional<Plan> plan = planRepository.findById(planId);
        return plan.map(p -> p.getTravel().getTravelName())
                .orElse(null);
    }
}
