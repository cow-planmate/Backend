package com.example.planmate.domain.shared.service.sync.dto;

import java.util.List;
import java.util.Map;

public record TimeTableSyncSummary(
        Map<Integer, Integer> insertedTempIdToRealId,
        List<Integer> updatedIds,
        List<Integer> deletedIds
) {}
