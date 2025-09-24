package com.example.planmate.domain.shared.service.sync;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.shared.cache.PlanCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanSyncService {

    private final PlanRepository planRepository;
    private final PlanCache planCache;

    public Plan syncPlan(int planId) {
        Plan cached = planCache.findPlanByPlanId(planId);
        return planRepository.save(cached);
    }
}
