package com.example.planmate.domain.shared.sync.adapters;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TravelCache;
import com.example.planmate.domain.shared.lazydto.PlanDto;
import com.example.planmate.domain.shared.sync.ports.PlanCommandPort;
import com.example.planmate.domain.travel.entity.Travel;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaPlanCommandAdapter implements PlanCommandPort {
    private final PlanCache planCache;
    private final TravelCache travelCache;

    @Override
    public UpdateResult update(UpdateRequest request) {
        Plan plan = planCache.findById(request.planId()) // JPA 스타일로 변경!
            .orElseThrow(() -> new IllegalStateException("Plan not found in cache: " + request.planId()));
        String planName = null;
        Integer travelId = null;
        String travelName = null;
        Integer adult = null;
        Integer child = null;
        String departure = request.departure();
        Integer transportationCategoryId = null;

        if (request.planName() != null) {
            plan.changePlanName(request.planName());
            planName = plan.getPlanName();
        }
        if (request.travelId() != null) {
            Travel travel = travelCache.findTravelByTravelId(request.travelId());
            plan.changeTravel(travel);
            travelId = travel.getTravelId();
            travelName = travel.getTravelName();
        }
        if (request.adultCount() != null || request.childCount() != null) {
            int adultCount = request.adultCount() != null ? request.adultCount() : plan.getAdultCount();
            int childCount = request.childCount() != null ? request.childCount() : plan.getChildCount();
            plan.updateCounts(adultCount, childCount);
            adult = adultCount;
            child = childCount;
        }
        if (request.departure() != null) {
            plan.changeDeparture(request.departure());
        }
        if (request.transportationCategoryId() != null) {
            plan.changeTransportationCategory(new TransportationCategory(request.transportationCategoryId()));
            transportationCategoryId = request.transportationCategoryId();
        }
        planCache.save(PlanDto.fromEntity(plan));

        return new UpdateResult(planName, travelId, travelName, adult, child, departure, transportationCategoryId);
    }
}
