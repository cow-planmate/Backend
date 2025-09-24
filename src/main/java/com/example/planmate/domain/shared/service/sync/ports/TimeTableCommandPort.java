package com.example.planmate.domain.shared.service.sync.ports;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public interface TimeTableCommandPort {
    // Upsert timetables for a plan, returning tempId(<0)->newId map and updated ids
    record UpsertRequest(int planId, List<Item> items) {
        public record Item(Integer idOrTempId, LocalDate date, LocalTime start, LocalTime end) {}
    }

    record UpsertResult(Map<Integer, Integer> insertedIdMap, List<Integer> updatedIds, List<Integer> deletedIds) {}

    UpsertResult upsert(UpsertRequest request);
}
