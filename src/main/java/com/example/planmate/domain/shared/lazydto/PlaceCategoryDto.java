package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.plan.entity.PlaceCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class PlaceCategoryDto {

    private Integer placeCategoryId;
    private String placeCategoryName;

    public static PlaceCategoryDto fromEntity(PlaceCategory placeCategory) {
        return PlaceCategoryDto.builder()
                .placeCategoryId(placeCategory.getPlaceCategoryId())
                .placeCategoryName(placeCategory.getPlaceCategoryName())
                .build();
    }

    public PlaceCategory toEntity() {
        return PlaceCategory.builder()
                .placeCategoryId(this.placeCategoryId)
                .placeCategoryName(this.placeCategoryName)
                .build();
    }

    public PlaceCategoryDto withPlaceCategoryId(Integer newId) {
        return this.toBuilder()
                .placeCategoryId(newId)
                .build();
    }
}