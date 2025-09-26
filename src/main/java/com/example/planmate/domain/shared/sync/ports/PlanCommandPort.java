package com.example.planmate.domain.shared.sync.ports;

public interface PlanCommandPort {
    record UpdateRequest(
            int planId,
            String planName,
            Integer travelId,
            Integer adultCount,
            Integer childCount,
            String departure,
            Integer transportationCategoryId
    ) {}

    record UpdateResult(
            String planName,
            Integer travelId,
            String travelName,
            Integer adultCount,
            Integer childCount,
            String departure,
            Integer transportationCategoryId
    ) {}

    UpdateResult update(UpdateRequest request);
}
