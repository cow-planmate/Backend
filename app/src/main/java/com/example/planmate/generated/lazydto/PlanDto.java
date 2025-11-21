package com.example.planmate.generated.lazydto;

import com.sharedsync.framework.shared.framework.annotation.*;
import lombok.*;
import com.sharedsync.framework.shared.framework.dto.CacheDto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

@Cache
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@AutoEntityConverter(repositories = {"userRepository", "transportationCategoryRepository", "travelRepository"})


public class PlanDto extends CacheDto<Integer> {

    @CacheId
    private Integer planId;
    private String planName;
    private String departure;
    private int adultCount;
    private int childCount;
    private Integer userId;
    private Integer transportationCategoryId;
    private Integer travelId;

    public static PlanDto fromEntity(Plan plan) {
        return new PlanDto(
                plan.getPlanId(),
                plan.getPlanName(),
                plan.getDeparture(),
                plan.getAdultCount(),
                plan.getChildCount(),
                plan.getUser().getUserId(),
                plan.getTransportationCategory().getTransportationCategoryId(),
                plan.getTravel().getTravelId()
        );
    }

    @EntityConverter
    public Plan toEntity(User user, TransportationCategory transportationCategory, Travel travel) {
        return Plan.builder()
                .planId(this.planId)
                .planName(this.planName)
                .departure(this.departure)
                .adultCount(this.adultCount)
                .childCount(this.childCount)
                .user(user)
                .transportationCategory(transportationCategory)
                .travel(travel)
                .build();
    }
}