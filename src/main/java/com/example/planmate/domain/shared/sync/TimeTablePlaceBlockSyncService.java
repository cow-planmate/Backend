package com.example.planmate.domain.shared.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeTablePlaceBlockSyncService {

    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;

    public void syncTimeTablePlaceBlocks(TimeTableSyncService.TimeTableSyncResult ttResult) {
        Map<Integer, TimeTable> changeTimeTable = ttResult.getChangeTimeTable();
        Map<Integer, TimeTable> notChangeTimeTable = ttResult.getNotChangeTimeTable();

        List<TimeTablePlaceBlock> newBlocks = new ArrayList<>();
        List<TimeTablePlaceBlock> deletedBlocks = new ArrayList<>();

        // 신규 타임테이블(tempId<0) -> 블록 전부 신규 저장
        for (Map.Entry<Integer, TimeTable> entry : changeTimeTable.entrySet()) {
            int tempId = entry.getKey();
            TimeTable realTimetable = entry.getValue();

            List<TimeTablePlaceBlock> blocks =
                    timeTablePlaceBlockCache.deleteByParentId(tempId);

            if (blocks != null) {
                for (TimeTablePlaceBlock block : blocks) {
                    if (block == null) continue;
                    block.assignTimeTable(realTimetable);
                    block.changeId(null);
                    newBlocks.add(block);
                }
            }
        }

        // 기존 타임테이블(id>=0) -> 수정/추가/삭제 처리
        for (Map.Entry<Integer, TimeTable> entry : notChangeTimeTable.entrySet()) {
            int timeTableId = entry.getKey();
            TimeTable realTimetable = entry.getValue();

            List<TimeTablePlaceBlock> oldBlocks =
                    timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);

            List<TimeTablePlaceBlock> blocks =
                    timeTablePlaceBlockCache.deleteByParentId(timeTableId);

            if (blocks != null) {
                for (TimeTablePlaceBlock block : blocks) {
                    if (block.getBlockId() != null && block.getBlockId() >= 0) {
                        TimeTablePlaceBlock existing = timeTablePlaceBlockRepository.findById(block.getBlockId())
                                .orElseThrow(() -> new IllegalArgumentException("블록을 찾을 수 없습니다. ID=" + block.getBlockId()));
                        existing.copyFrom(block);
                        // 삭제 대상에서 제외
                        oldBlocks.removeIf(ot ->
                                ot.getBlockId() != null && ot.getBlockId().equals(existing.getBlockId())
                        );
                    } else {
                        block.assignTimeTable(realTimetable);
                        block.changeId(null);
                        newBlocks.add(block);
                    }
                }
            }
            // 캐시에 없던 나머지 oldBlocks는 삭제
            deletedBlocks.addAll(oldBlocks);
        }

        timeTablePlaceBlockRepository.deleteAll(deletedBlocks);
        timeTablePlaceBlockRepository.saveAll(newBlocks);
    }
}
