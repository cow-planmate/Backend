package com.example.planmate.domain.shared.service.sync.adapters;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.shared.service.sync.ports.PlanQueryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaPlanQueryAdapter implements PlanQueryPort {
    private final PlanRepository planRepository;

    @Override
    public boolean existsActivePlan(int planId) {
        return planRepository.existsById(planId); // TODO: replace with isActive if available
    }
}
