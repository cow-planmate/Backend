package com.example.planmate.domain.shared.service.sync.framework.steps;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.shared.service.sync.PlanSyncService;
import com.example.planmate.domain.shared.service.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.service.sync.framework.TargetedSyncStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SyncPlanStep implements TargetedSyncStep {
    private final PlanSyncService planSyncService;

    @Override
    public void execute(PlanSyncContext ctx) {
        int planId = ctx.getPlanId();
        Plan saved = planSyncService.syncPlan(planId);
        ctx.setSavedPlan(saved);
    }

}
