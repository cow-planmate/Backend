package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.shared.cache.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.cache.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.cache.annotation.CacheEntity;
import com.example.planmate.domain.shared.cache.annotation.CacheId;
import com.example.planmate.domain.shared.cache.annotation.EntityConverter;
import com.example.planmate.domain.shared.enums.ECasheKey;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

@CacheEntity(keyType = ECasheKey.PLAN)
@AutoRedisTemplate("planRedis")
@AutoEntityConverter(repositories = {"userRepository", "transportationCategoryRepository", "travelRepository"})
public record PlanDto(
        @CacheId
        Integer planId,
        String planName,
        String departure,
        int adultCount,
        int childCount,
        Integer userId,
        Integer transportationCategoryId,
        Integer travelId    
) {
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