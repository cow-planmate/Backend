package com.example.planmate.domain.shared.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.shared.enums.ECasheKey;
import com.example.planmate.domain.shared.lazydto.PlanDto;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanCache {

    private final PlanRepository planRepository;
    private final RedisTemplate<String, PlanDto> planRedis;
    private final UserRepository userRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelRepository travelRepository;

    

    public Plan insertPlanByKey(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(ECasheKey.PLAN.key(planId), PlanDto.fromEntity(plan));
        return plan;
    }
    public Plan findPlanByPlanId(int planId) {
        PlanDto dto = planRedis.opsForValue().get(ECasheKey.PLAN.key(planId));
        if (dto == null) return null;
        User userRef = userRepository.getReferenceById(dto.userId());
        TransportationCategory tcRef = transportationCategoryRepository.getReferenceById(dto.transportationCategoryId());
        Travel travelRef = travelRepository.getReferenceById(dto.travelId());
        return dto.toEntity(userRef, tcRef, travelRef);
    }
    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(ECasheKey.PLAN.key(plan.getPlanId()), PlanDto.fromEntity(plan));
    }
    public void deletePlan(int planId) {
        planRedis.delete(ECasheKey.PLAN.key(planId));
    }


    


}
