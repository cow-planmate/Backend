package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;
import com.example.planmate.move.shared.framework.annotation.CacheEntity;
import com.example.planmate.move.shared.framework.annotation.CacheId;
import com.example.planmate.move.shared.framework.dto.CacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TravelDto extends CacheDto<Integer> {

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

    public Travel toEntity(TravelCategory travelCategory) {
        return Travel.builder()
                .travelId(this.travelId)
                .travelName(this.travelName)
                .travelCategory(travelCategory)
                .build();
    }
}