package com.example.planmate.domain.shared.service.sync.adapters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.service.sync.TimeTablePlaceBlockSyncService;
import com.example.planmate.domain.shared.service.sync.TimeTableSyncService;
import com.example.planmate.domain.shared.service.sync.ports.TimeTablePlaceBlockCommandPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaTimeTablePlaceBlockCommandAdapter implements TimeTablePlaceBlockCommandPort {
    private final TimeTablePlaceBlockSyncService blockSyncService;

    @Override
    public void upsert(UpsertRequest request, Map<Integer, Integer> tempToRealTimeTableIdMap) {
        // 현재 구현은 캐시 기반 동기화 서비스를 재사용하므로, 서비스가 필요로 하는
        // TimeTableSyncResult 형태를 ids로부터 합성해서 전달합니다.

        Map<Integer, TimeTable> changeTimeTable = new HashMap<>();      // tempId(<0) -> stub(TimeTable with realId)
        Map<Integer, TimeTable> notChangeTimeTable = new HashMap<>();   // realId(>=0) -> stub(TimeTable with realId)

        // tempId -> realId 매핑을 changeTimeTable로 변환
        for (Map.Entry<Integer, Integer> e : tempToRealTimeTableIdMap.entrySet()) {
            changeTimeTable.put(e.getKey(), TimeTable.builder().timeTableId(e.getValue()).build());
        }

        // 요청 내 아이템들 중 real timetable id 사용한 것들을 notChange로 반영
        if (request != null && request.items() != null) {
            for (var item : request.items()) {
                Integer ttIdOrTemp = item.timeTableIdOrTemp();
                if (ttIdOrTemp != null && ttIdOrTemp >= 0) {
                    notChangeTimeTable.putIfAbsent(ttIdOrTemp, TimeTable.builder().timeTableId(ttIdOrTemp).build());
                }
            }
        }

        // delete ids는 블록 동기화에서는 사용하지 않으므로 빈 리스트 전달
        List<Integer> deleteIds = List.of();

        var synthetic = new TimeTableSyncService.TimeTableSyncResult(changeTimeTable, notChangeTimeTable, deleteIds);
        blockSyncService.syncTimeTablePlaceBlocks(synthetic);
    }
}
