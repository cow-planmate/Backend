package com.example.planmate.domain.shared.sync.framework;

import java.util.List;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.shared.sync.TimeTableSyncService.TimeTableSyncResult;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PlanSyncContext {
    private final int planId;

    private Plan savedPlan;
    private TimeTableSyncResult timeTableSyncResult;
    private List<Integer> deletedTimeTableRedisKeys;
}
