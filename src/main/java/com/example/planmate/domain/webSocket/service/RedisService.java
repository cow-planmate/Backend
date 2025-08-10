package com.example.planmate.domain.webSocket.service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import jakarta.annotation.PostConstruct;
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
    private final TravelRepository travelRepository;
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
    private final RedisTemplate<String, Travel> travelRedis;
    private final String TRAVEL_PREFIX = "TRAVEL";
    private final RedisTemplate<String, PlaceCategory> placeCategoryRedis;
    private final String PLACECATEGORY_PREFIX = "PLACECATEGORY";
    private final PlaceCategoryRepository placeCategoryRepository;

    @PostConstruct
    public void init() {
        List<Travel> travels = travelRepository.findAll();
        for(Travel travel : travels) {
            travelRedis.opsForValue().set(TRAVEL_PREFIX +travel.getTravelId(), travel);
        }
        List<PlaceCategory> placeCategories = placeCategoryRepository.findAll();
        for(PlaceCategory placeCategory : placeCategories) {
            placeCategoryRedis.opsForValue().set(PLACECATEGORY_PREFIX +placeCategory.getPlaceCategoryId(), placeCategory);
        }
    }
    public void registerPlan(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(PLAN_PREFIX + planId, plan);
        //테스트용
        Plan cached = planRedis.opsForValue().get(PLAN_PREFIX + planId);
        List<TimeTable> timeTables = registerTimeTable(plan.getPlanId());
        for(TimeTable timeTable : timeTables){
            registerTimeTablePlaceBlock(timeTable.getTimeTableId());
        }
    }
    public Plan getPlan(int planId) {
        Plan cached = planRedis.opsForValue().get(PLAN_PREFIX + planId);
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

    public List<TimeTable> deleteTimeTableByPlanId(int planId) {
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        planToTimeTableRedis.delete(PLANTOTIMETABLE_PREFIX + planId);
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
    public int registerNewTimeTable(int planId, TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndDecrement();
        timetable.setTimeTableId(tempId);
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timetable.getTimeTableId(), timetable);
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        timeTableIds.add(timetable.getTimeTableId());
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
        return tempId;
    }
    public void deleteRedisTimeTable(int timetableId) {
        timeTableRedis.delete(TIMETABLE_PREFIX + timetableId);
        List<Integer> timeTablePlaceBlockIds = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timetableId);
        if(timeTablePlaceBlockIds != null) {
            for(Integer timeTablePlaceBlockId : timeTablePlaceBlockIds){
                timeTablePlaceBlockRedis.delete(TIMETABLE_PREFIX + timeTablePlaceBlockId);
            }
        }
    }
    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) {
        for(TimetableVO timeTable : timeTableVOs){
            timeTableRedis.delete(TIMETABLE_PREFIX + timeTable.getTimetableId());
            List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
            timeTableIds.remove(timeTable.getTimetableId());
            planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
            List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timeTable.getTimetableId());
            if(timeTablePlaceBlocks != null) {
                timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlocks);
            }
        }
    }
    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timeTable.getTimeTableId(), timeTable);
    }

    public List<TimeTablePlaceBlock> getTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        if(timeTablePlaceBlocks != null) {
            List<String> keys = new ArrayList<>();
            for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
                keys.add(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
            }
            return timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        }
        return null;
    }

    public List<TimeTablePlaceBlock> deleteTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        timeTableToTimeTablePlaceBlockRedis.delete(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlocks);
        if(timeTablePlaceBlocks != null) {
            List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
            for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
                keys.add(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
            }
            return timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        }
        return null;
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

    public int registerNewTimeTablePlaceBlock(int timeTableId, TimeTablePlaceBlock block) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndDecrement();
        block.setBlockId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), block);
        List<Integer> timeTableBlockIds = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        if(timeTableBlockIds==null) timeTableBlockIds = new ArrayList<>();
        timeTableBlockIds.add(block.getBlockId());
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTableBlockIds);
        return tempId;
    }

    public void deleteTimeTablePlaceBlock(int timeTableId, int blockId) {
        timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX +blockId);
        List<Integer> timeTablePlaceBlockIds = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        timeTablePlaceBlockIds.remove(Integer.valueOf(blockId));
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTablePlaceBlockIds);
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX +block.getBlockId(), block);
    }

    public Travel getTravelByTravelId(int travelId) {
        return travelRedis.opsForValue().get(TRAVEL_PREFIX + travelId);
    }

    public PlaceCategory getPlaceCategory(int placeCategoryId) {return  placeCategoryRedis.opsForValue().get(PLACECATEGORY_PREFIX+ placeCategoryId);}
}
