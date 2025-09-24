package com.example.planmate.domain.shared.service.sync.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.service.sync.TimeTableSyncService;
import com.example.planmate.domain.shared.service.sync.ports.TimeTableCommandPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaTimeTableCommandAdapter implements TimeTableCommandPort {
    private final TimeTableSyncService timeTableSyncService;

    @Override
    public UpsertResult upsert(UpsertRequest request) {
        // Reuse existing cache-driven sync: read from Redis by planId, apply to DB
        var result = timeTableSyncService.syncTimeTables(request.planId());

        // Map existing TimeTableSyncResult to port DTO
        Map<Integer, Integer> inserted = new HashMap<>();
        List<Integer> updated = new ArrayList<>();
        List<Integer> deleted = new ArrayList<>();

        // When current TimeTableSyncService returns entity maps, adapt accordingly.
        // changeTimeTable: tempId(<0) -> entity(with new id)
        result.getChangeTimeTable().forEach((tempId, entity) -> inserted.put(tempId, entity.getTimeTableId()));
        // notChangeTimeTable: existing id -> entity
        updated.addAll(result.getNotChangeTimeTable().keySet());
        // delete ids for cache cleanup already provided
        deleted.addAll(result.getDeleteTimeTableIds());

        return new UpsertResult(inserted, updated, deleted);
    }
}
