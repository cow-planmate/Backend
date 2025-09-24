package com.example.planmate.domain.shared.service.sync.framework.steps;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.service.sync.TimeTablePlaceBlockSyncService;
import com.example.planmate.domain.shared.service.sync.framework.PlanSyncContext;
import com.example.planmate.domain.shared.service.sync.framework.TargetedSyncStep;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SyncTimeTablePlaceBlockStep implements TargetedSyncStep {
    private final TimeTablePlaceBlockSyncService timeTablePlaceBlockSyncService;

    @Override
    public void execute(PlanSyncContext ctx) {
        timeTablePlaceBlockSyncService.syncTimeTablePlaceBlocks(ctx.getTimeTableSyncResult());
    }

}
