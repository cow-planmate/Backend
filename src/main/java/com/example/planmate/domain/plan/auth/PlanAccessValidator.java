package com.example.planmate.domain.plan.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.infrastructure.redis.PlanCacheService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanAccessValidator {
    private final PlanRepository planRepository;
    private final PlanEditorRepository planEditorRepository;
    private final PlanCacheService planCacheService;
    public Plan validateUserHasAccessToPlan(int userId, int planId) {
        Plan plan = planCacheService.get(planId);

        if (plan == null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("없는 일정입니다"));
        }

        boolean isOwner = plan.getUser().getUserId() == userId;
        boolean isEditor = planEditorRepository.existsByUserUserIdAndPlanPlanId(userId, planId);

        if (!isOwner && !isEditor) {
            throw new AccessDeniedException("요청 권한이 없습니다");
        }
        return plan;
    }
    public void checkUserAccessToPlan(int userId, int planId) {
        Plan plan = planCacheService.get(planId);

        if (plan == null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("없는 일정입니다"));
        }

        boolean isOwner = plan.getUser().getUserId() == userId;
        boolean isEditor = planEditorRepository.existsByUserUserIdAndPlanPlanId(userId, planId);

        if (!isOwner && !isEditor) {
            throw new AccessDeniedException("요청 권한이 없습니다");
        }
    }
}
