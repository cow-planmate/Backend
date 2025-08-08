package com.example.planmate.service;

import com.example.planmate.plan.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.plan.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
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
        Plan plan = redisService.getPlan(planId);
        planRepository.save(plan);

        List<TimeTable> timetableList = redisService.getTimeTableByPlanId(planId);
        Map<Integer, TimeTable> tempIdToSaved = new HashMap<>();

        for (TimeTable t : timetableList) {
            tempIdToSaved.put(t.getTimeTableId(), t);  // 임시 ID 저장
            t.setTimeTableId(null);  // ID auto-generation
            t.setPlan(plan);
        }
        List<TimeTable> savedTimetables = timeTableRepository.saveAll(timetableList);

        Map<Integer, TimeTable> realMap = new HashMap<>();
        for (int i = 0; i < timetableList.size(); i++) {
            Integer tempId = tempIdToSaved.get(i).getTimeTableId();
            realMap.put(tempId, savedTimetables.get(i));
        }

        List<TimeTablePlaceBlock> allBlocks = new ArrayList<>();

        for (Map.Entry<Integer, TimeTable> entry : realMap.entrySet()) {
            Integer tempId = entry.getKey();
            TimeTable realTimetable = entry.getValue();

            List<TimeTablePlaceBlock> blocks = redisService.getTimeTablePlaceBlockByTimeTableId(tempId);
            for (TimeTablePlaceBlock block : blocks) {
                block.setTimeTable(realTimetable);
                block.setBlockId(null);
            }
            allBlocks.addAll(blocks);
        }
        timeTablePlaceBlockRepository.saveAll(allBlocks);
    }





}
