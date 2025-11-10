package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;
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
public class TravelDto {
    @CacheId
    private Integer travelId;
    private String travelName;
    private Integer travelCategoryId;

    public static TravelDto fromEntity(Travel travel) {
        return new TravelDto(
                travel.getTravelId(),
                travel.getTravelName(),
                travel.getTravelCategory().getTravelCategoryId()
        );
    }
    @EntityConverter
    public Travel toEntity(TravelCategory travelCategory) {
        return Travel.builder()
                .travelId(this.travelId)
                .travelName(this.travelName)
                .travelCategory(travelCategory)
                .build();
    }
}