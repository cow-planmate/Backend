package com.example.planmate.domain.redis.cache;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanCacheService {
    private final RedisTemplate<String, PlanDto> planRedis;
    private static final String PLAN_PREFIX = "PLAN";

    private final PlanRepository planRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelRepository travelRepository;
    private final UserRepository userRepository;
    private final TimeTableCacheService timeTableCacheService;
    private final TimeTablePlaceBlockCacheService placeBlockCacheService;

    public void registerPlan(int planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(PLAN_PREFIX + planId, PlanDto.fromEntity(plan));
        // cascade register timetables and blocks
        timeTableCacheService.registerTimeTable(plan.getPlanId()).forEach(dto -> {
            placeBlockCacheService.register(dto.timeTableId());
        });
    }

    public Plan getPlan(int planId) {
        PlanDto dto = planRedis.opsForValue().get(PLAN_PREFIX + planId);
        if (dto == null) return null;
        User userRef = userRepository.getReferenceById(dto.userId());
        TransportationCategory tcRef = transportationCategoryRepository.getReferenceById(dto.transportationCategoryId());
        Travel travelRef = travelRepository.getReferenceById(dto.travelId());
        return dto.toEntity(userRef, tcRef, travelRef);
    }

    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(PLAN_PREFIX + plan.getPlanId(), PlanDto.fromEntity(plan));
    }

    public void deletePlan(int planId) {
        planRedis.delete(PLAN_PREFIX + planId);
    }
}
