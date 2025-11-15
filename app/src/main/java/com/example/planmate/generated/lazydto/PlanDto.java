package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;
import com.sharedsync.framework.shared.framework.annotation.AutoEntityConverter;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.sharedsync.framework.shared.framework.dto.CacheDto;

import com.sharedsync.framework.shared.presence.annotation.PresenceKey;
import com.sharedsync.framework.shared.presence.annotation.PresenceRoot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity 
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@AutoRedisTemplate("planRedis") //이름매칭으로 대체 가능할듯
@AutoEntityConverter(repositories = {"userRepository", "transportationCategoryRepository", "travelRepository"})
@PresenceRoot(
        channel = "plan",     // WebSocket channel 이름
        idField = "planId"    // Root 식별자
)

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