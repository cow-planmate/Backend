package com.example.planmate.auth;

import com.example.planmate.entity.Plan;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlanAccessValidator {

    private final PlanRepository planRepository;
    public Plan validateUserHasAccessToPlan(int userId, int planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        if (plan.getUser().getUserId() != userId) {
            throw new AccessDeniedException("요청 권한이 없습니다");
        }

        return plan;
    }
}
