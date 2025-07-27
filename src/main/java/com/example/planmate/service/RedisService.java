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
    private final RedisTemplate<Integer, TimeTable> timetableRedis;
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    @Getter
    private final RedisTemplate<Integer, TimeTablePlaceBlock> timetablePlaceBlockRedis;

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
        return plan;
    }
    public Plan registerPlan(Plan plan){
        planRedis.opsForValue().set(plan.getPlanId(), plan);
        return plan;
    }
    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(plan.getPlanId(), plan);
    }

    public TimeTable getTimeTable(int timetableId) {
        TimeTable cached = timetableRedis.opsForValue().get(timetableId);

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다.");
        }
        return cached;
    }

    public TimeTable registerTimeTable(int timetableId) {
        TimeTable timeTable = timeTableRepository.findById(timetableId)
                .orElseThrow(() -> new IllegalArgumentException("TimeTable not found: " + timetableId));
        timetableRedis.opsForValue().set(timetableId, timeTable);
        return timeTable;
    }
    public int registerTimeTable(TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndIncrement();
        timetable.setTimeTableId(tempId);
        timetableRedis.opsForValue().set(timetable.getTimeTableId(), timetable);
        return tempId;
    }

    public void deleteTimeTable(int timetableId) {
        timetableRedis.delete(timetableId);
    }

    public void updateTimeTable(TimeTable timeTable) {
        timetableRedis.opsForValue().set(timeTable.getTimeTableId(), timeTable);
    }


    public TimeTablePlaceBlock getTimeTablePlaceBlock(int blockId) {
        TimeTablePlaceBlock cached = timetablePlaceBlockRedis.opsForValue().get(blockId);

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다.");
        }
        return cached;
    }

    public TimeTablePlaceBlock registerTimeTablePlaceBlock(int blockId) {
        TimeTablePlaceBlock block = timeTablePlaceBlockRepository.findById(blockId)
                .orElseThrow(() -> new IllegalArgumentException("TimeTablePlaceBlock not found: " + blockId));
        timetablePlaceBlockRedis.opsForValue().set(blockId, block);
        return block;
    }

    public TimeTablePlaceBlock registerTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timetablePlaceBlockRedis.opsForValue().set(block.getBlockId(), block);
        return block;
    }

    public void deleteTimeTablePlaceBlock(int blockId) {
        timetablePlaceBlockRedis.delete(blockId);
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timetablePlaceBlockRedis.opsForValue().set(block.getBlockId(), block);
    }
}
