package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.shared.framework.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.framework.annotation.CacheEntity;
import com.example.planmate.domain.shared.framework.annotation.CacheId;
import com.example.planmate.domain.shared.framework.annotation.EntityConverter;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@CacheEntity
@AutoRedisTemplate("planRedis")
@AutoEntityConverter(repositories = {"userRepository", "transportationCategoryRepository", "travelRepository"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class PlanDto {

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
        return PlanDto.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .departure(plan.getDeparture())
                .adultCount(plan.getAdultCount())
                .childCount(plan.getChildCount())
                .userId(plan.getUser().getUserId())
                .transportationCategoryId(plan.getTransportationCategory().getTransportationCategoryId())
                .travelId(plan.getTravel().getTravelId())
                .build();
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

    public PlanDto withPlanId(Integer newPlanId) {
        return this.toBuilder()
                .planId(newPlanId)
                .build();
    }
}