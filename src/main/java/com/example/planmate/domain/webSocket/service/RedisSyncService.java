package com.example.planmate.domain.webSocket.service;

import com.example.planmate.common.exception.PlanNotFoundException;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final RedisService redisService;

    public void syncPlanToDatabase(int planId) {
        if(!planRepository.existsById(planId)) {
            //plan이 없을때 예외
            throw new PlanNotFoundException();
        }
        
        // Redis에서 Plan 데이터 가져오기
        Plan redisPlan = redisService.findPlanByPlanId(planId);
        
        // 기존 DB Plan 조회 후 필드만 업데이트 (cascade 문제 방지)
        Plan existingPlan = planRepository.findById(planId)
            .orElseThrow(() -> new PlanNotFoundException());
        
        // Plan 필드들만 업데이트 (TimeTable 컬렉션은 건드리지 않음)
        existingPlan.updateFromRedis(redisPlan);
        Plan savedPlan = planRepository.save(existingPlan);

        List<TimeTable> timetableList = redisService.deleteTimeTableByPlanId(planId);
        List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);
        Map<Integer, TimeTable> tempIdToEntity = new HashMap<>();
        for (TimeTable t : timetableList) {
             // 기존 ID 보관
            //새로운 테이블
            int tempId = t.getTimeTableId();
            if(t.getTimeTableId()<0){
                t.changeId(null);
                // ensure plan reference
                t = TimeTable.builder()
                        .timeTableId(null)
                        .date(t.getDate())
                        .timeTableStartTime(t.getTimeTableStartTime())
                        .timeTableEndTime(t.getTimeTableEndTime())
                        .plan(savedPlan)
                        .build();
            }
            //기존 테이블
            else{
                TimeTable timeTable = timeTableRepository.findById(t.getTimeTableId()).orElse(null);
                if(timeTable!=null){
                    timeTable.changeDate(t.getDate());
                    timeTable.changeTime(t.getTimeTableStartTime(), t.getTimeTableEndTime());
                    oldTimetables.removeIf(ot ->
                            ot.getTimeTableId() != null && ot.getTimeTableId().equals(timeTable.getTimeTableId())
                    );
                }
            }
            timeTableRepository.save(t);
            tempIdToEntity.put(tempId, t);
        }
        timeTableRepository.deleteAll(oldTimetables);

        Map<Integer, TimeTable> changeTimeTable = new HashMap<>();
        Map<Integer, TimeTable> notChangeTimeTable = new HashMap<>();
        tempIdToEntity.forEach((tempId, oldT) -> {
            (tempId >= 0 ? notChangeTimeTable : changeTimeTable).put(tempId, oldT);
        });

        List<TimeTablePlaceBlock> newBlocks = new ArrayList<>();
        List<Integer> deletTimeTableIds = new ArrayList<>();
        for (Map.Entry<Integer, TimeTable> entry : changeTimeTable.entrySet()) {
            int timeTableId = entry.getKey();
            deletTimeTableIds.add(timeTableId);
            TimeTable realTimetable = tempIdToEntity.get(timeTableId);
            List<TimeTablePlaceBlock> blocks = redisService.deleteTimeTablePlaceBlockByTimeTableId(timeTableId);
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
        List<TimeTablePlaceBlock> deletedBlocks = new ArrayList<>();
        for (Map.Entry<Integer, TimeTable> entry : notChangeTimeTable.entrySet()) {
            int timeTableId = entry.getKey();
            deletTimeTableIds.add(timeTableId);
            TimeTable realTimetable = timeTableRepository.findById(timeTableId).orElse(null);
            List<Integer> blockIds = new ArrayList<>();
            List<TimeTablePlaceBlock> blocks = redisService.deleteTimeTablePlaceBlockByTimeTableId(timeTableId);
            if(blocks != null && !blocks.isEmpty()) {
                for (TimeTablePlaceBlock block : blocks) {
                    if(block.getBlockId() >= 0){
                        TimeTablePlaceBlock timeTablePlaceBlock = timeTablePlaceBlockRepository.findById(block.getBlockId()).orElseThrow(() -> new IllegalArgumentException("블록을 찾을 수 없습니다. ID=" + block.getBlockId()));
                        timeTablePlaceBlock.copyFrom(block);
                        blockIds.add(block.getBlockId());

                    }
                    else {
                        block.assignTimeTable(realTimetable);
                        block.changeId(null);
                        newBlocks.add(block);
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
        redisService.deleteRedisTimeTable(deletTimeTableIds);
        timeTablePlaceBlockRepository.saveAll(newBlocks);
        redisService.deletePlan(planId);
    }
}