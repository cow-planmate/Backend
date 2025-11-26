package com.example.planmate.domain.plan.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.sharedsync.shared.auth.StompAccessValidator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanStompAccessValidator implements StompAccessValidator {

    private final PlanAccessValidator planAccessValidator;

    @Override
    public boolean supports(String destination) {
        return destination != null && destination.contains("/plan/");
    }

    @Override
    public void validate(int userId, String destination) {
        Integer planId = extractPlanId(destination);
        if (planId == null) {
            throw new AccessDeniedException("Plan id missing in destination");
        }
        planAccessValidator.checkUserAccessToPlan(userId, planId);
    }

    private Integer extractPlanId(String dest) {
        String[] parts = dest.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("plan".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException ignore) {
                    return null;
                }
            }
        }
        return null;
    }
}

