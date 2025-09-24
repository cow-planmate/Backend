package com.example.planmate.domain.shared.service.sync.framework.steps;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.service.sync.TimeTableSyncService;
import com.example.planmate.domain.shared.service.sync.TimeTableSyncService.TimeTableSyncResult;
import com.example.planmate.domain.shared.service.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.service.sync.framework.TargetedSyncStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SyncTimeTableStep implements TargetedSyncStep {
    private final TimeTableSyncService timeTableSyncService;

    @Override
    public void execute(PlanSyncContext ctx) {
        TimeTableSyncResult result = timeTableSyncService.syncTimeTables(ctx.getPlanId());
        ctx.setTimeTableSyncResult(result);
        ctx.setDeletedTimeTableRedisKeys(result.getDeleteTimeTableIds());
    }

}
