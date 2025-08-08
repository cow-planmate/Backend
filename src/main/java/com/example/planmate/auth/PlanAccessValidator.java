package com.example.planmate.auth;

import com.example.planmate.plan.entity.Plan;
import com.example.planmate.plan.repository.PlanEditorRepository;
import com.example.planmate.plan.repository.PlanRepository;
import com.example.planmate.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanAccessValidator {
    private final PlanRepository planRepository;
    private final PlanEditorRepository planEditorRepository;
    private final RedisService redisService;
    public Plan validateUserHasAccessToPlan(int userId, int planId) {
        Plan plan = redisService.getPlan(planId);

        if (plan == null) {
            plan = planRepository.findById(planId)
                    .orElseThrow(() -> new RuntimeException("플랜 없음"));
        }

        boolean isOwner = plan.getUser().getUserId() == userId;
        boolean isEditor = planEditorRepository.existsByUserUserIdAndPlanPlanId(userId, planId);

        if (!isOwner && !isEditor) {
            throw new AccessDeniedException("요청 권한이 없습니다");
        }
        return plan;
    }
}
