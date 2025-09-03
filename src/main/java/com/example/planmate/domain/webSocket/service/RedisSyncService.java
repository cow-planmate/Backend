package com.example.planmate.domain.webSocket.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.exception.PlanNotFoundException;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisSyncService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final RedisService redisService;

    @Transactional
    public void syncPlanToDatabase(int planId) {
        if(!planRepository.existsById(planId)) {
            //plan이 없을때 예외
            throw new PlanNotFoundException();
        }
        //plan save
        planRepository.save(redisService.getPlan(planId));

        List<TimeTable> timetableList = redisService.deleteTimeTableByPlanId(planId);
        List<TimeTable> newTimetables = new ArrayList<>();
        List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);
        Map<Integer, TimeTable> tempIdToEntity = new HashMap<>();
        for (TimeTable t : timetableList) {
            tempIdToEntity.put(t.getTimeTableId(), t); // 기존 ID 보관
            //새로운 테이블
            if(t.getTimeTableId()<0){
                t.setTimeTableId(null);
                newTimetables.add(t);
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
        }
        timeTableRepository.saveAll(newTimetables);
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
            TimeTable realTimetable = entry.getValue();
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
            TimeTable realTimetable = entry.getValue();
            List<TimeTablePlaceBlock> oldBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
            List<TimeTablePlaceBlock> blocks = redisService.deleteTimeTablePlaceBlockByTimeTableId(timeTableId);
            if(blocks != null && !blocks.isEmpty()) {
                for (TimeTablePlaceBlock block : blocks) {
                    if(block.getBlockId() >= 0){
                        TimeTablePlaceBlock timeTablePlaceBlock = timeTablePlaceBlockRepository.findById(block.getBlockId()).orElseThrow(() -> new IllegalArgumentException("블록을 찾을 수 없습니다. ID=" + block.getBlockId()));
                        timeTablePlaceBlock.copyFrom(block);
                        oldBlocks.removeIf(ot ->
                                ot.getBlockId() != null && ot.getBlockId().equals(timeTablePlaceBlock.getBlockId())
                        );
                    }
                    else {
                        block.assignTimeTable(realTimetable);
                        block.changeId(null);
                        newBlocks.add(block);
                    }
                }
            }
            deletedBlocks.addAll(oldBlocks);
        }
        timeTablePlaceBlockRepository.deleteAll(deletedBlocks);
        redisService.deleteRedisTimeTable(deletTimeTableIds);
        timeTablePlaceBlockRepository.saveAll(newBlocks);
        redisService.deletePlan(planId);
    }
}
