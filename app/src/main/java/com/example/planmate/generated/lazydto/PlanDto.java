package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityReference;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;

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
public class PlanDto extends EntityBackedCacheDto<Integer, Plan> {

    @CacheId
    private Integer planId;
    private String planName;
    private String departure;
    private int adultCount;
    private int childCount;
    @EntityReference(repository = "userRepository", entityType = User.class, optional = false)
    private Integer userId;
    @EntityReference(repository = "transportationCategoryRepository", entityType = TransportationCategory.class, optional = false)
    private Integer transportationCategoryId;
    @EntityReference(repository = "travelRepository", entityType = Travel.class, optional = false)
    private Integer travelId;

    public static PlanDto fromEntity(Plan plan) {
        return instantiateFromEntity(plan, PlanDto.class);
    }
}