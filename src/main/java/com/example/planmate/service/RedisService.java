package com.example.planmate.service;

import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.Getter;
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
    @Getter
    private final RedisTemplate<Integer, Plan> planRedis;
    @Getter
    private final RedisTemplate<Integer, TimeTable> timeTableRedis;
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<Integer, List<Integer>> planToTimeTableRedis;
    @Getter
    private final RedisTemplate<Integer, TimeTablePlaceBlock> timeTablePlaceBlockRedis;
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<Integer, List<Integer>> timeTableToTimeTablePlaceBlockRedis;

    public Plan getPlan(int planId) {
        Plan cached = planRedis.opsForValue().get(planId);
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 Plan 정보가 없습니다.");
        }
        return cached;
    }
    public Plan registerPlan(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(planId, plan);
        List<TimeTable> timeTables = registerTimeTable(plan.getPlanId());
        for(TimeTable timeTable : timeTables){
            registerTimeTablePlaceBlock(timeTable.getTimeTableId());
        }
        return plan;
    }
    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(plan.getPlanId(), plan);
    }




    public TimeTable getTimeTable(int timetableId) {
        TimeTable cached = timeTableRedis.opsForValue().get(timetableId);

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다.");
        }
        return cached;
    }

    public List<TimeTable> getTimeTableByPlanId(int planId) {
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(planId);
        return timeTableRedis.opsForValue().multiGet(timeTableIds);
    }

    public List<TimeTable> registerTimeTable(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<Integer> timeTableIds = new ArrayList<>();
        for(TimeTable timeTable : timeTables){
            timeTableRedis.opsForValue().set(timeTable.getTimeTableId(), timeTable);
            timeTableIds.add(timeTable.getTimeTableId());
        }
        planToTimeTableRedis.opsForValue().set(planId, timeTableIds);
        return timeTables;
    }
    public int registerNewTimeTable(TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndIncrement();
        timetable.setTimeTableId(tempId);
        timeTableRedis.opsForValue().set(timetable.getTimeTableId(), timetable);
        return tempId;
    }

    public void deleteTimeTable(int timetableId) {
        timeTableRedis.delete(timetableId);
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timetableId);
        if(timeTablePlaceBlocks != null) {
            timeTablePlaceBlockRedis.delete(timeTablePlaceBlocks);
        }
    }

    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(timeTable.getTimeTableId(), timeTable);
    }




    public List<TimeTablePlaceBlock> getTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timetableId);
        return timeTablePlaceBlockRedis.opsForValue().multiGet(timeTablePlaceBlocks);
    }

    public TimeTablePlaceBlock getTimeTablePlaceBlock(int blockId) {
        TimeTablePlaceBlock cached = timeTablePlaceBlockRedis.opsForValue().get(blockId);
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다.");
        }
        return cached;
    }


    public List<TimeTablePlaceBlock> registerTimeTablePlaceBlock(int timeTableId) {
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<Integer> timeTablePlaceBlockIds = new ArrayList<>();
        for(TimeTablePlaceBlock timeTablePlaceBlock : timeTablePlaceBlocks){
            timeTablePlaceBlockRedis.opsForValue().set(timeTablePlaceBlock.getBlockId(), timeTablePlaceBlock);
            timeTablePlaceBlockIds.add(timeTablePlaceBlock.getBlockId());
        }

        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(timeTableId, timeTablePlaceBlockIds);
        return timeTablePlaceBlocks;
    }

    public TimeTablePlaceBlock registerNewTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndIncrement();
        block.setBlockId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(block.getBlockId(), block);
        return block;
    }

    public void deleteTimeTablePlaceBlock(int blockId) {
        timeTablePlaceBlockRedis.delete(blockId);
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timeTablePlaceBlockRedis.opsForValue().set(block.getBlockId(), block);
    }
}
