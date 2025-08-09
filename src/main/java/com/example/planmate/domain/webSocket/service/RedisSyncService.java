package com.example.planmate.domain.webSocket.service;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void syncPlanToDatabase(int planId) {
        Plan plan = redisService.getPlan(planId);
        planRepository.save(plan);

        List<TimeTable> timetableList = redisService.deleteTimeTableByPlanId(planId);

        Map<Integer, TimeTable> tempIdToEntity = new HashMap<>();
        for (TimeTable t : timetableList) {
            tempIdToEntity.put(t.getTimeTableId(), t); // 기존 ID 보관
            if(t.getTimeTableId()<0){
                t.setTimeTableId(null);
            }
        }

        List<TimeTable> savedTimetables = new ArrayList<>();
        List<TimeTable> newTimetables = new ArrayList<>();
        List<TimeTable> oldTimetables = timeTableRepository.findByPlanPlanId(planId);

        for(TimeTable t : timetableList){

            //원래 있던거
            if(t.getTimeTableId()!=null&&t.getTimeTableId()>=0){
                TimeTable timeTable = timeTableRepository.findById(t.getTimeTableId()).orElse(null);
                if(timeTable!=null){
                    timeTable.setDate(t.getDate());
                    timeTable.setTimeTableEndTime(t.getTimeTableEndTime());
                    timeTable.setTimeTableStartTime(t.getTimeTableStartTime());
                    savedTimetables.add(timeTable);
                    oldTimetables.removeIf(ot ->
                            ot.getTimeTableId() != null && ot.getTimeTableId().equals(timeTable.getTimeTableId())
                    );
                }
            }
            //새로운 거
            else{
                newTimetables.add(t);
                savedTimetables.add(t);
            }
            timeTableRepository.saveAll(newTimetables);
        }
        timeTableRepository.deleteAll(oldTimetables);

        Map<Integer, TimeTable> realMap = new HashMap<>();
        for (Map.Entry<Integer, TimeTable> entry : tempIdToEntity.entrySet()) {
            int tempId = entry.getKey();
            TimeTable oldT = entry.getValue();
            for (TimeTable saved : savedTimetables) {
                if (saved != null && saved.equals(oldT)) {
                    realMap.put(tempId, saved);
                    break;
                }
            }
        }

        List<TimeTablePlaceBlock> newBlocks = new ArrayList<>();

        for (Map.Entry<Integer, TimeTable> entry : realMap.entrySet()) {
            int tempId = entry.getKey();
            TimeTable realTimetable = entry.getValue();
            List<TimeTablePlaceBlock> oldBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(entry.getKey());
            List<TimeTablePlaceBlock> blocks = redisService.deleteTimeTablePlaceBlockByTimeTableId(tempId);
            if(blocks != null && !blocks.isEmpty()) {
                for (TimeTablePlaceBlock block : blocks) {
                    if(block.getBlockId() >= 0){
                        TimeTablePlaceBlock timeTablePlaceBlock = timeTablePlaceBlockRepository.findById(block.getBlockId()).orElse(null);
                        timeTablePlaceBlock.setBlock(block);
                        oldBlocks.removeIf(ot ->
                                ot.getBlockId() != null && ot.getBlockId().equals(timeTablePlaceBlock.getBlockId())
                        );
                    }
                    else {
                        block.setTimeTable(realTimetable);
                        block.setBlockId(null);
                        newBlocks.add(block);
                    }
                }
                redisService.deleteRedisTimeTable(tempId);
                timeTablePlaceBlockRepository.saveAll(newBlocks);
                timeTablePlaceBlockRepository.deleteAll(oldBlocks);
            }
        }
        redisService.deletePlan(planId);
    }




}
