package com.example.planmate.domain.shared.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.planmate.common.exception.PlanNotFoundException;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;

    public void syncPlanToDatabase(int planId) {
        if(!planRepository.existsById(planId)) {
            throw new PlanNotFoundException();
        }
        
        // Cache에서 Plan 데이터 가져오기
        Optional<Plan> cachedPlanOpt = planCache.findById(planId);
        if (cachedPlanOpt.isEmpty()) {
            return; // 캐시에 없으면 동기화할 것이 없음
        }
        
        // 기존 DB Plan 조회 후 필드만 업데이트 (cascade 문제 방지)
        Plan existingPlan = planRepository.findById(planId)
            .orElseThrow(() -> new PlanNotFoundException());
        
        // Cache의 데이터로 Plan 업데이트 (기존 메서드 사용)
        Plan cachedPlan = cachedPlanOpt.get();
        if (cachedPlan.getPlanName() != null) {
            existingPlan.changePlanName(cachedPlan.getPlanName());
        }
        if (cachedPlan.getDeparture() != null) {
            existingPlan.changeDeparture(cachedPlan.getDeparture());
        }
        existingPlan.updateCounts(cachedPlan.getAdultCount(), cachedPlan.getChildCount());
        if (cachedPlan.getTransportationCategory() != null) {
            existingPlan.changeTransportationCategory(cachedPlan.getTransportationCategory());
        }
        if (cachedPlan.getTravel() != null) {
            existingPlan.changeTravel(cachedPlan.getTravel());
        }
        Plan savedPlan = planRepository.save(existingPlan);

        // TimeTable 동기화
        List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);
        Map<Integer, TimeTable> tempIdToEntity = new HashMap<>();
        
        // Cache에서 TimeTable DTO 로드 후 Entity로 변환
        List<TimeTable> cachedTimeTables = timeTableCache.loadFromDatabase(planId).stream()
            .map(dto -> TimeTable.builder()
                .timeTableId(dto.timeTableId())
                .date(dto.date())
                .timeTableStartTime(dto.timeTableStartTime())
                .timeTableEndTime(dto.timeTableEndTime())
                .plan(savedPlan)
                .build())
            .toList();
        
        for (TimeTable t : cachedTimeTables) {
            int tempId = t.getTimeTableId();
            TimeTable timeTable;
            
            // 새로운 테이블 (음수 ID)
            if(tempId < 0){
                timeTable = TimeTable.builder()
                        .timeTableId(null)
                        .date(t.getDate())
                        .timeTableStartTime(t.getTimeTableStartTime())
                        .timeTableEndTime(t.getTimeTableEndTime())
                        .plan(savedPlan)
                        .build();
            }
            // 기존 테이블 (양수 ID)
            else{
                TimeTable existingTimeTable = timeTableRepository.findById(tempId).orElse(null);
                if(existingTimeTable != null){
                    existingTimeTable.changeDate(t.getDate());
                    existingTimeTable.changeTime(t.getTimeTableStartTime(), t.getTimeTableEndTime());
                    oldTimetables.removeIf(ot ->
                            ot.getTimeTableId() != null && ot.getTimeTableId().equals(existingTimeTable.getTimeTableId())
                    );
                    timeTable = existingTimeTable;
                } else {
                    continue;
                }
            }
            timeTableRepository.save(timeTable);
            tempIdToEntity.put(tempId, timeTable);
        }
        timeTableRepository.deleteAll(oldTimetables);

        // TimeTablePlaceBlock 동기화
        Map<Integer, TimeTable> changeTimeTable = new HashMap<>();
        Map<Integer, TimeTable> notChangeTimeTable = new HashMap<>();
        tempIdToEntity.forEach((tempId, oldT) -> {
            (tempId >= 0 ? notChangeTimeTable : changeTimeTable).put(tempId, oldT);
        });

        List<TimeTablePlaceBlock> newBlocks = new ArrayList<>();
        List<TimeTablePlaceBlock> deletedBlocks = new ArrayList<>();
        
        // 새로 생성된 TimeTable의 블록들 처리
        for (Map.Entry<Integer, TimeTable> entry : changeTimeTable.entrySet()) {
            int timeTableId = entry.getKey();
            TimeTable realTimetable = entry.getValue();
            
            // Cache에서 블록들 가져오기 (이미 캐시에 로드되어 있음)
            List<TimeTablePlaceBlock> blocks = timeTablePlaceBlockCache.findByParentId(timeTableId);
            
            if(blocks != null && !blocks.isEmpty()) {
                for (TimeTablePlaceBlock block : blocks) {
                    if(block != null) {
                        block.assignTimeTable(realTimetable);
                        block.changeId(null);
                        newBlocks.add(block);
                    }
                }
            }
        }
        
        // 기존 TimeTable의 블록들 처리
        for (Map.Entry<Integer, TimeTable> entry : notChangeTimeTable.entrySet()) {
            int timeTableId = entry.getKey();
            List<Integer> blockIds = new ArrayList<>();
            
            // Cache에서 블록들 가져오기 (이미 캐시에 로드되어 있음)
            List<TimeTablePlaceBlock> blocks = timeTablePlaceBlockCache.findByParentId(timeTableId);
            
            if(blocks != null && !blocks.isEmpty()) {
                for (TimeTablePlaceBlock block : blocks) {
                    if(block.getBlockId() >= 0){
                        TimeTablePlaceBlock timeTablePlaceBlock = timeTablePlaceBlockRepository.findById(block.getBlockId())
                            .orElseThrow(() -> new IllegalArgumentException("블록을 찾을 수 없습니다. ID=" + block.getBlockId()));
                        timeTablePlaceBlock.copyFrom(block);
                        blockIds.add(block.getBlockId());
                    } else {
                        TimeTable realTimetable = timeTableRepository.findById(timeTableId).orElse(null);
                        if (realTimetable != null) {
                            block.assignTimeTable(realTimetable);
                            block.changeId(null);
                            newBlocks.add(block);
                        }
                    }
                }
            }
            
            List<TimeTablePlaceBlock> oldBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
            for(int blockId : blockIds){
                oldBlocks.removeIf(oldBlock -> oldBlock.getBlockId() == blockId);
            }
            deletedBlocks.addAll(oldBlocks);
        }
        
        timeTablePlaceBlockRepository.deleteAll(deletedBlocks);
        timeTablePlaceBlockRepository.saveAll(newBlocks);
        
        // 캐시 정리
        planCache.deleteById(planId);
        for (Integer timeTableId : tempIdToEntity.keySet()) {
            timeTableCache.deleteById(timeTableId);
            // TimeTablePlaceBlock 캐시도 같이 정리 (Redis에서 DTO만 조회)
            timeTablePlaceBlockCache.findDtosByParentId(timeTableId).forEach(dto -> {
                if (dto.blockId() != null) {
                    timeTablePlaceBlockCache.deleteById(dto.blockId());
                }
            });
        }
    }
}