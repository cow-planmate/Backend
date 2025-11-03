package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;

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
public class TravelDto {

    private Integer travelId;
    private String travelName;
    private Integer travelCategoryId;

    public static TravelDto fromEntity(Travel travel) {
        return TravelDto.builder()
                .travelId(travel.getTravelId())
                .travelName(travel.getTravelName())
                .travelCategoryId(travel.getTravelCategory().getTravelCategoryId())
                .build();
    }

    public Travel toEntity(TravelCategory travelCategory) {
        return Travel.builder()
                .travelId(this.travelId)
                .travelName(this.travelName)
                .travelCategory(travelCategory)
                .build();
    }
}