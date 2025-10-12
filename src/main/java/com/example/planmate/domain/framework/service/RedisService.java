package com.example.planmate.domain.framework.service;

import com.example.planmate.domain.framework.annotation.SocketRoot;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.enums.ECasheKey;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final PlanRepository planRepository;
    private final TravelRepository travelRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final UserRepository userRepository;

    private final RedisTemplate<String, PlanDto> planRedis;

    // =========================
    // ✅ 루트 엔터티 공통 진입점
    // =========================

    @SuppressWarnings("unchecked")
    public <E> E findById(Class<E> rootClass, int id) {
        if (!rootClass.isAnnotationPresent(SocketRoot.class))
            throw new IllegalArgumentException(rootClass.getSimpleName() + " is not a @SocketRoot entity");

        if (rootClass.equals(Plan.class)) {
            return (E) findPlanByPlanId(id);
        }

        throw new UnsupportedOperationException("RedisService.findById() not implemented for: " + rootClass.getSimpleName());
    }

    public <E> void updateEntity(Class<E> rootClass, E entity) {
        if (!rootClass.isAnnotationPresent(SocketRoot.class))
            throw new IllegalArgumentException(rootClass.getSimpleName() + " is not a @SocketRoot entity");

        if (rootClass.equals(Plan.class)) {
            updatePlan((Plan) entity);
            return;
        }

        throw new UnsupportedOperationException("RedisService.updateEntity() not implemented for: " + rootClass.getSimpleName());
    }

    public <E> void deleteEntity(Class<E> rootClass, int id) {
        if (!rootClass.isAnnotationPresent(SocketRoot.class))
            throw new IllegalArgumentException(rootClass.getSimpleName() + " is not a @SocketRoot entity");

        if (rootClass.equals(Plan.class)) {
            deletePlan(id);
            return;
        }

        throw new UnsupportedOperationException("RedisService.deleteEntity() not implemented for: " + rootClass.getSimpleName());
    }

    // =========================
    // ✅ Plan 전용 로직 (루트 엔터티)
    // =========================

    private Plan findPlanByPlanId(int planId) {
        PlanDto dto = planRedis.opsForValue().get(ECasheKey.PLAN.key(planId));
        if (dto == null) return null;

        User userRef = userRepository.getReferenceById(dto.userId());
        TransportationCategory tcRef = transportationCategoryRepository.getReferenceById(dto.transportationCategoryId());
        Travel travelRef = travelRepository.getReferenceById(dto.travelId());
        return dto.toEntity(userRef, tcRef, travelRef);
    }

    private void updatePlan(Plan plan) {
        planRedis.opsForValue().set(ECasheKey.PLAN.key(plan.getPlanId()), PlanDto.fromEntity(plan));
    }

    private void deletePlan(int planId) {
        planRedis.delete(ECasheKey.PLAN.key(planId));
    }
}