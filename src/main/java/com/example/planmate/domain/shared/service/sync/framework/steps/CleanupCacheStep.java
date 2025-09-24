package com.example.planmate.domain.shared.service.sync.framework.steps;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.service.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.service.sync.framework.SyncStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CleanupCacheStep implements SyncStep<PlanSyncContext> {
    private final TimeTableCache timeTableCache;
    private final PlanCache planCache;

    @Override
    public void execute(PlanSyncContext ctx) {
        List<Integer> deleteIds = ctx.getDeletedTimeTableRedisKeys();
        if (deleteIds != null && !deleteIds.isEmpty()) {
            timeTableCache.deleteRedisTimeTable(deleteIds);
        }
        planCache.deletePlan(ctx.getPlanId());
    }
}
