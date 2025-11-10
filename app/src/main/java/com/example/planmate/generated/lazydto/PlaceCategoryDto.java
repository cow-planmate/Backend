package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlaceCategoryDto {
    @CacheId
    private Integer placeCategoryId;
    private String placeCategoryName;

    public static PlaceCategoryDto fromEntity(PlaceCategory placeCategory) {
        return new PlaceCategoryDto(
                placeCategory.getPlaceCategoryId(),
                placeCategory.getPlaceCategoryName()
        );
    }
    @EntityConverter
    public PlaceCategory toEntity() {
        return PlaceCategory.builder()
                .placeCategoryId(this.placeCategoryId)
                .placeCategoryName(this.placeCategoryName)
                .build();
    }
}