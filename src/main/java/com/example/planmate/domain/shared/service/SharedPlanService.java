package com.example.planmate.domain.shared.service;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TravelCache;
import com.example.planmate.domain.shared.dto.WPlanRequest;
import com.example.planmate.domain.shared.dto.WPlanResponse;
import com.example.planmate.domain.travel.entity.Travel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedPlanService {
    private final PlanCache planCache;
    private final TravelCache travelCache;

    public WPlanResponse updatePlan(int planId, WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        Plan plan = planCache.findPlanByPlanId(planId);

        if(request.getPlanName() != null) {
            plan.changePlanName(request.getPlanName());
            response.setPlanName(plan.getPlanName());
        }

        if(request.getTravelId() != null) {
            Travel travel = travelCache.findTravelByTravelId(request.getTravelId());
            plan.changeTravel(travel);
            response.setTravelId(travel.getTravelId());
            response.setTravelName(travel.getTravelName());
        }
        if (request.getAdultCount() != null || request.getChildCount() != null) {
            int adult = request.getAdultCount() != null ? request.getAdultCount() : plan.getAdultCount();
            int child = request.getChildCount() != null ? request.getChildCount() : plan.getChildCount();
            plan.updateCounts(adult, child);

            response.setAdultCount(adult);
            response.setChildCount(child);
        }
        if(request.getDeparture() != null) {
            plan.changeDeparture(request.getDeparture());
            response.setDeparture(request.getDeparture());
        }
        if(request.getTransportationCategoryId() != null) {
            plan.changeTransportationCategory(new TransportationCategory(request.getTransportationCategoryId()));
            response.setTransportationCategoryId(request.getTransportationCategoryId());
        }
        planCache.updatePlan(plan);
        return response;
    }
}
