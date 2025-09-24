package com.example.planmate.domain.shared.service.sync.ports;

import java.util.List;

public interface TimeTablePlaceBlockCommandPort {
    // Upsert blocks for timetables using either tempId(<0) or real timetable id
    record BlockItem(Integer blockIdOrTemp, Integer timeTableIdOrTemp, String placeId,
                     String memo, Integer orderIndex, Integer durationMinutes) {}

    record UpsertRequest(List<BlockItem> items) {}

    void upsert(UpsertRequest request, java.util.Map<Integer, Integer> tempToRealTimeTableIdMap);
}
