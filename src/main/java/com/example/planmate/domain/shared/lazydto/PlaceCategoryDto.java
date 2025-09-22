package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.plan.entity.PlaceCategory;

public record PlaceCategoryDto(
        Integer placeCategoryId,
        String placeCategoryName
) {
    public static PlaceCategoryDto fromEntity(PlaceCategory placeCategory) {
        return new PlaceCategoryDto(
                placeCategory.getPlaceCategoryId(),
                placeCategory.getPlaceCategoryName()
        );
    }

    public PlaceCategory toEntity() {
        return PlaceCategory.builder()
                .placeCategoryId(this.placeCategoryId)
                .placeCategoryName(this.placeCategoryName)
                .build();
    }
}