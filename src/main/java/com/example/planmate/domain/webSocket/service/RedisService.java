package com.example.planmate.domain.webSocket.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.enums.ECasheKey;
import com.example.planmate.domain.webSocket.lazydto.PlaceCategoryDto;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.webSocket.lazydto.TravelDto;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TravelRepository travelRepository;
    private final RedisTemplate<String, PlanDto> planRedis;
    private final RedisTemplate<String, TimeTableDto> timeTableRedis;
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, Integer> planToTimeTableRedis;
    private final RedisTemplate<String, TimeTablePlaceBlockDto> timeTablePlaceBlockRedis;
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, Integer> timeTableToTimeTablePlaceBlockRedis;
    private final RedisTemplate<String, TravelDto> travelRedis;
    private final RedisTemplate<String, PlaceCategoryDto> placeCategoryRedis;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelCategoryRepository travelCategoryRepository;
    private final UserRepository userRepository;
    private final PlacePhotoRepository placePhotoRepository;

    @PostConstruct
    public void init() {
        List<Travel> travels = travelRepository.findAll();
        for(Travel travel : travels) {
            travelRedis.opsForValue().set(ECasheKey.TRAVEL.key(travel.getTravelId()), TravelDto.fromEntity(travel));
        }
        List<PlaceCategory> placeCategories = placeCategoryRepository.findAll();
        for(PlaceCategory placeCategory : placeCategories) {
            placeCategoryRedis.opsForValue().set(ECasheKey.PLACECATEGORY.key(placeCategory.getPlaceCategoryId()), PlaceCategoryDto.fromEntity(placeCategory));
        }
    }

    public void insertPlan(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(ECasheKey.PLAN.key(planId), PlanDto.fromEntity(plan));
        List<TimeTableDto> timeTables = insertTimeTable(plan.getPlanId());
        for(TimeTableDto timeTable : timeTables){
            insertTimeTablePlaceBlock(timeTable.timeTableId());
        }
    }
    public Plan findPlanByPlanId(int planId) {
    PlanDto dto = planRedis.opsForValue().get(ECasheKey.PLAN.key(planId));
        if (dto == null) return null;
        User userRef = userRepository.getReferenceById(dto.userId());
        TransportationCategory tcRef = transportationCategoryRepository.getReferenceById(dto.transportationCategoryId());
        Travel travelRef = travelRepository.getReferenceById(dto.travelId());
        return dto.toEntity(userRef, tcRef, travelRef);
    }
    public void updatePlan(Plan plan) {
    planRedis.opsForValue().set(ECasheKey.PLAN.key(plan.getPlanId()), PlanDto.fromEntity(plan));
    }
    public void deletePlan(int planId) {
    planRedis.delete(ECasheKey.PLAN.key(planId));
    }

    public TimeTable findTimeTableByTimeTableId(int timetableId) {
    TimeTableDto cached = timeTableRedis.opsForValue().get(ECasheKey.TIMETABLE.key(timetableId));

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다: "+ timetableId);
        }
        Plan planRef = planRepository.getReferenceById(cached.planId());
        return cached.toEntity(planRef);
    }

    public List<TimeTable> findTimeTablesByPlanId(int planId) {
    Set<Integer> timeTableIds = planToTimeTableRedis.opsForSet().members(ECasheKey.PLANTOTIMETABLE.key(planId));
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(ECasheKey.TIMETABLE.key(timeTableId));
        }
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) {
            if (dto != null) result.add(dto.toEntity(planRef));
        }
        return result;
    }

    public List<TimeTable> deleteTimeTableByPlanId(int planId) {
    Set<Integer> timeTableIds = planToTimeTableRedis.opsForSet().members(ECasheKey.PLANTOTIMETABLE.key(planId));
    planToTimeTableRedis.delete(ECasheKey.PLANTOTIMETABLE.key(planId));
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(ECasheKey.TIMETABLE.key(timeTableId));
        }
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) {
            if (dto != null) result.add(dto.toEntity(planRef));
        }
        return result;
    }

    public List<TimeTableDto> insertTimeTable(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<TimeTableDto> result = new ArrayList<>();
        for(TimeTable timeTable : timeTables){
            TimeTableDto dto = TimeTableDto.fromEntity(timeTable);
            timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(dto.timeTableId()), dto);
            // Set에 추가
            planToTimeTableRedis.opsForSet().add(ECasheKey.PLANTOTIMETABLE.key(planId), dto.timeTableId());
            result.add(dto);
        }
        return result;
    }
    public int createTimeTable(int planId, TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndDecrement();
        timetable.changeId(tempId);
    timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(timetable.getTimeTableId()), TimeTableDto.fromEntity(timetable));
        // Set에 추가
    planToTimeTableRedis.opsForSet().add(ECasheKey.PLANTOTIMETABLE.key(planId), timetable.getTimeTableId());
        return tempId;
    }

    public void deleteRedisTimeTable(List<Integer> timetableIds) {
    List<String> timetableKeys = timetableIds.stream()
        .map(id -> ECasheKey.TIMETABLE.key(id))
                .toList();
        timeTableRedis.delete(timetableKeys);
        deleteRedisTimeTableBlockByTimeTableId(timetableIds);
    }

    private void deleteRedisTimeTableBlockByTimeTableId(List<Integer> timetableIds) {
    List<Integer> placeBlockIds = timetableIds.stream()
        .map(id -> timeTableToTimeTablePlaceBlockRedis.opsForSet()
            .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(id)))
        .filter(Objects::nonNull)
        .flatMap(Set::stream)
        .toList();

    List<String> placeBlockKeys = placeBlockIds.stream()
        .map(id -> ECasheKey.TIMETABLEPLACEBLOCK.key(id))
                .toList();
        timeTablePlaceBlockRedis.delete(placeBlockKeys);

    // 관계(Set) 키들도 함께 제거
    List<String> relationKeys = timetableIds.stream()
        .map(id -> ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(id))
        .toList();
    timeTableToTimeTablePlaceBlockRedis.delete(relationKeys);
    }

    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) {
        for(TimetableVO timeTable : timeTableVOs){
            if(timeTable.getTimetableId() != null){
                timeTableRedis.delete(ECasheKey.TIMETABLE.key(timeTable.getTimetableId()));
                // Set에서 제거
                planToTimeTableRedis.opsForSet().remove(ECasheKey.PLANTOTIMETABLE.key(planId), timeTable.getTimetableId());
                Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet()
                        .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTable.getTimetableId()));
                if(timeTablePlaceBlocks != null && !timeTablePlaceBlocks.isEmpty()) {
                    for(int timeTablePlaceBlockId : timeTablePlaceBlocks){
                        timeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
                    }
                    // 해당 타임테이블의 관계(Set) 키 제거
                    timeTableToTimeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTable.getTimetableId()));
                }
            }
        }
    }
    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(timeTable.getTimeTableId()), TimeTableDto.fromEntity(timeTable));
    }

    public List<TimeTablePlaceBlock> findTimeTablePlaceBlocksByTimeTableId(int timetableId) {
    Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet()
        .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timetableId));
        if(timeTablePlaceBlocks == null || timeTablePlaceBlocks.isEmpty()) return Collections.emptyList();

        List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
        for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
            keys.add(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
        }
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                PlacePhoto ppRef = dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null;
                result.add(dto.toEntity(pcRef, timeTableRef, ppRef));
            }
        }
        return result;
    }

    public List<TimeTablePlaceBlock> deleteTimeTablePlaceBlockByTimeTableId(int timetableId) {
    String key = ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timetableId);
        Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet().members(key);
        if(timeTablePlaceBlocks == null || timeTablePlaceBlocks.isEmpty()) return Collections.emptyList();

        List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
        for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
            keys.add(ECasheKey.TIMETABLEPLACEBLOCK.key(timeTablePlaceBlockId));
        }
        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);

        // 관계(Set) 키 제거
        timeTableToTimeTablePlaceBlockRedis.delete(key);

        if (dtos == null) return Collections.emptyList();
        TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
        List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
        for (TimeTablePlaceBlockDto dto : dtos) {
            if (dto != null) {
                PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                PlacePhoto ppRef = dto.placeId() != null ? placePhotoRepository.getReferenceById(dto.placeId()) : null;
                result.add(dto.toEntity(pcRef, timeTableRef, ppRef));
            }
        }
        return result;
    }

    public TimeTablePlaceBlock findTimeTablePlaceBlockByBlockId(int blockId) {
    TimeTablePlaceBlockDto cached = timeTablePlaceBlockRedis.opsForValue().get(ECasheKey.TIMETABLEPLACEBLOCK.key(blockId));
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다: " + blockId);
        }
        PlaceCategory pcRef = placeCategoryRepository.getReferenceById(cached.placeCategoryId());
        TimeTable ttRef = timeTableRepository.getReferenceById(cached.timeTableId());
        PlacePhoto ppRef = cached.placeId() != null ? placePhotoRepository.getReferenceById(cached.placeId()) : null;
        return cached.toEntity(pcRef, ttRef, ppRef);
    }


    public List<TimeTablePlaceBlockDto> insertTimeTablePlaceBlock(int timeTableId) {
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<TimeTablePlaceBlockDto> result = new ArrayList<>();
        for(TimeTablePlaceBlock timeTablePlaceBlock : timeTablePlaceBlocks){
            TimeTablePlaceBlockDto dto = TimeTablePlaceBlockDto.fromEntity(timeTablePlaceBlock);
            timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(dto.blockId()), dto);
            timeTableToTimeTablePlaceBlockRedis.opsForSet()
                    .add(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTableId), dto.blockId());
            result.add(dto);
        }
        return result;
    }

    public int createTimeTablePlaceBlock(int timeTableId, TimeTablePlaceBlock block) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndDecrement();
        block.changeId(tempId);
    timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(block.getBlockId()), TimeTablePlaceBlockDto.fromEntity(block));
    timeTableToTimeTablePlaceBlockRedis.opsForSet()
        .add(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTableId), block.getBlockId());
        return tempId;
    }

    public void deleteTimeTablePlaceBlock(int timeTableId, int blockId) {
    timeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLEPLACEBLOCK.key(blockId));
        timeTableToTimeTablePlaceBlockRedis.opsForSet()
        .remove(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTableId), blockId);
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
    timeTablePlaceBlockRedis.opsForValue().set(ECasheKey.TIMETABLEPLACEBLOCK.key(block.getBlockId()), TimeTablePlaceBlockDto.fromEntity(block));
    }

    public Travel findTravelByTravelId(int travelId) {
    TravelDto dto = travelRedis.opsForValue().get(ECasheKey.TRAVEL.key(travelId));
        if (dto == null) return null;
        return dto.toEntity(travelCategoryRepository.getReferenceById(dto.travelCategoryId()));
    }

    public PlaceCategory findPlaceCategoryByPlaceCategoryId(int placeCategoryId) {
    PlaceCategoryDto dto = placeCategoryRedis.opsForValue().get(ECasheKey.PLACECATEGORY.key(placeCategoryId));
        if (dto == null) return placeCategoryRepository.getReferenceById(placeCategoryId);
        return dto.toEntity();
    }
}
