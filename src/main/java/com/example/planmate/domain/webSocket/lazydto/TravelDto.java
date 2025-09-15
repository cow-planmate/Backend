package com.example.planmate.domain.webSocket.lazydto;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;

public record TravelDto(
        Integer travelId,
        String travelName,
        Integer travelCategoryId
) {
    public static TravelDto fromEntity(Travel travel) {
        return new TravelDto(
                travel.getTravelId(),
                travel.getTravelName(),
                travel.getTravelCategory().getTravelCategoryId()
        );
    }

    public Travel toEntity(TravelCategory travelCategory) {
        return Travel.builder()
                .travelId(this.travelId)
                .travelName(this.travelName)
                .travelCategory(travelCategory)
                .build();
    }
}