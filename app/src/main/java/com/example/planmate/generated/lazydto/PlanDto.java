package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.sharedsync.framework.shared.framework.annotation.AutoEntityConverter;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

@CacheEntity 
@AutoRedisTemplate("planRedis") //이름매칭으로 대체 가능할듯
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

    /**
     * ID만 변경된 새로운 PlanDto 객체를 생성합니다.
     */
    public PlanDto withPlanId(Integer newPlanId) {
        return new PlanDto(
                newPlanId,
                this.planName,
                this.departure,
                this.adultCount,
                this.childCount,
                this.userId,
                this.transportationCategoryId,
                this.travelId
        );
    }
}