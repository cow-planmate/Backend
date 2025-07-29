package com.example.planmate.service;

import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final RedisTemplate<String, Plan> planRedis;
    private final String PLAN_PREFIX = "PLAN";
    private final RedisTemplate<String, TimeTable> timeTableRedis;
    private final String TIMETABLE_PREFIX = "TIMETABLE";
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, List<Integer>> planToTimeTableRedis;
    private final String PLANTOTIMETABLE_PREFIX = "PLANTOTIMETABLE";
    private final RedisTemplate<String, TimeTablePlaceBlock> timeTablePlaceBlockRedis;
    private final String TIMETABLEPLACEBLOCK_PREFIX = "TIMETABLEPLACEBLOCK";
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, List<Integer>> timeTableToTimeTablePlaceBlockRedis;
    private final String TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX = "TIMETABLETOTIMETABLEPLACEBLOCK";


    public void registerPlan(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(PLAN_PREFIX + planId, plan);
        List<TimeTable> timeTables = registerTimeTable(plan.getPlanId());
        for(TimeTable timeTable : timeTables){
            registerTimeTablePlaceBlock(timeTable.getTimeTableId());
        }
    }
    public Plan getPlan(int planId) {
        Plan cached = planRedis.opsForValue().get(PLAN_PREFIX + planId);
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 Plan 정보가 없습니다.");
        }
        return cached;
    }
    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(PLAN_PREFIX + plan.getPlanId(), plan);
    }
    public void deletePlan(int planId) {
        planRedis.delete(PLAN_PREFIX + planId);
    }

    public TimeTable getTimeTable(int timetableId) {
        TimeTable cached = timeTableRedis.opsForValue().get(TIMETABLE_PREFIX + timetableId);

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다.");
        }
        return cached;
    }

    public List<TimeTable> getTimeTableByPlanId(int planId) {
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(TIMETABLE_PREFIX + timeTableId);
        }
        return timeTableRedis.opsForValue().multiGet(keys);
    }

    public List<TimeTable> registerTimeTable(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<Integer> timeTableIds = new ArrayList<>();
        for(TimeTable timeTable : timeTables){
            timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timeTable.getTimeTableId(), timeTable);
            timeTableIds.add(timeTable.getTimeTableId());
        }
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
        return timeTables;
    }
    public int registerNewTimeTable(TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndIncrement();
        timetable.setTimeTableId(tempId);
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timetable.getTimeTableId(), timetable);
        return tempId;
    }

    public void deleteTimeTable(int timetableId) {
        timeTableRedis.delete(TIMETABLE_PREFIX + timetableId);
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timetableId);
        if(timeTablePlaceBlocks != null) {
            timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlocks);
        }
    }

    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timeTable.getTimeTableId(), timeTable);
    }




    public List<TimeTablePlaceBlock> getTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
        for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
           keys.add(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
        }
        return timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
    }

    public TimeTablePlaceBlock getTimeTablePlaceBlock(int blockId) {
        TimeTablePlaceBlock cached = timeTablePlaceBlockRedis.opsForValue().get(TIMETABLEPLACEBLOCK_PREFIX + blockId);
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다.");
        }
        return cached;
    }


    public List<TimeTablePlaceBlock> registerTimeTablePlaceBlock(int timeTableId) {
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<Integer> timeTablePlaceBlockIds = new ArrayList<>();
        for(TimeTablePlaceBlock timeTablePlaceBlock : timeTablePlaceBlocks){
            timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlock.getBlockId(), timeTablePlaceBlock);
            timeTablePlaceBlockIds.add(timeTablePlaceBlock.getBlockId());
        }
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTablePlaceBlockIds);
        return timeTablePlaceBlocks;
    }

    public TimeTablePlaceBlock registerNewTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndIncrement();
        block.setBlockId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), block);
        return block;
    }

    public void deleteTimeTablePlaceBlock(int blockId) {
        timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX +blockId);
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX +block.getBlockId(), block);
    }
}
