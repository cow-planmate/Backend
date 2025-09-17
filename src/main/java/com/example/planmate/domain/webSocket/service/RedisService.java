package com.example.planmate.domain.webSocket.service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.webSocket.lazydto.PlaceCategoryDto;
import com.example.planmate.domain.webSocket.lazydto.PlanDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.webSocket.lazydto.TravelDto;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.travel.repository.TravelCategoryRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TravelRepository travelRepository;
    private final RedisTemplate<String, PlanDto> planRedis;
    private final String PLAN_PREFIX = "PLAN";
    private final RedisTemplate<String, TimeTableDto> timeTableRedis;
    private final String TIMETABLE_PREFIX = "TIMETABLE";
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, List<Integer>> planToTimeTableRedis;
    private final String PLANTOTIMETABLE_PREFIX = "PLANTOTIMETABLE";
    private final RedisTemplate<String, TimeTablePlaceBlockDto> timeTablePlaceBlockRedis;
    private final String TIMETABLEPLACEBLOCK_PREFIX = "TIMETABLEPLACEBLOCK";
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, List<Integer>> timeTableToTimeTablePlaceBlockRedis;
    private final String TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX = "TIMETABLETOTIMETABLEPLACEBLOCK";
    private final RedisTemplate<String, TravelDto> travelRedis;
    private final String TRAVEL_PREFIX = "TRAVEL";
    private final RedisTemplate<String, PlaceCategoryDto> placeCategoryRedis;
    private final String PLACECATEGORY_PREFIX = "PLACECATEGORY";
    private final PlaceCategoryRepository placeCategoryRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelCategoryRepository travelCategoryRepository;
    private final String USERID_NICKNAME_PREFIX = "USERIDNICKNAME";
    private final RedisTemplate<String, String> userIdNicknameRedis;
    private final String NICKNAME_USERID_PREFIX = "NICKNAMEUSERID";
    private final RedisTemplate<String, Integer> nicknameUseridRedis;
    private final UserRepository userRepository;

    private final RedisTemplate<String, String> planTrackerRedis;
    private final String PLANTRACKER_PREFIX = "PLANTRACKER";

    private final RedisTemplate<String, Integer> userIdToPlanIdRedis;
    private final String USERIDTOPLANID_PREFIX = "USERIDTOPLANID";

    private final String REFRESHTOKEN_PREFIX = "REFRESHTOKEN";
    private final RedisTemplate<String, Integer> refreshTokenRedis;

    @PostConstruct
    public void init() {
        List<Travel> travels = travelRepository.findAll();
        for(Travel travel : travels) {
            travelRedis.opsForValue().set(TRAVEL_PREFIX + travel.getTravelId(), TravelDto.fromEntity(travel));
        }
        List<PlaceCategory> placeCategories = placeCategoryRepository.findAll();
        for(PlaceCategory placeCategory : placeCategories) {
            placeCategoryRedis.opsForValue().set(PLACECATEGORY_PREFIX + placeCategory.getPlaceCategoryId(), PlaceCategoryDto.fromEntity(placeCategory));
        }
    }


    public void registerPlan(int planId){
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        planRedis.opsForValue().set(PLAN_PREFIX + planId, PlanDto.fromEntity(plan));
        List<TimeTableDto> timeTables = registerTimeTable(plan.getPlanId());
        for(TimeTableDto timeTable : timeTables){
            registerTimeTablePlaceBlock(timeTable.timeTableId());
        }
    }
    public Plan getPlan(int planId) {
        PlanDto dto = planRedis.opsForValue().get(PLAN_PREFIX + planId);
        if (dto == null) return null;
        User userRef = userRepository.getReferenceById(dto.userId());
        TransportationCategory tcRef = transportationCategoryRepository.getReferenceById(dto.transportationCategoryId());
        Travel travelRef = travelRepository.getReferenceById(dto.travelId());
        return dto.toEntity(userRef, tcRef, travelRef);
    }
    public void updatePlan(Plan plan) {
        planRedis.opsForValue().set(PLAN_PREFIX + plan.getPlanId(), PlanDto.fromEntity(plan));
    }
    public void deletePlan(int planId) {
        planRedis.delete(PLAN_PREFIX + planId);
    }

    public TimeTable getTimeTable(int timetableId) {
        TimeTableDto cached = timeTableRedis.opsForValue().get(TIMETABLE_PREFIX + timetableId);

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다.");
        }
        Plan planRef = planRepository.getReferenceById(cached.planId());
        return cached.toEntity(planRef);
    }

    public List<TimeTable> getTimeTableByPlanId(int planId) {
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(TIMETABLE_PREFIX + timeTableId);
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
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        planToTimeTableRedis.delete(PLANTOTIMETABLE_PREFIX + planId);
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(TIMETABLE_PREFIX + timeTableId);
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

    public List<TimeTableDto> registerTimeTable(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<Integer> timeTableIds = new ArrayList<>();
        List<TimeTableDto> result = new ArrayList<>();
        for(TimeTable timeTable : timeTables){
            TimeTableDto dto = TimeTableDto.fromEntity(timeTable);
            timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + dto.timeTableId(), dto);
            timeTableIds.add(dto.timeTableId());
            result.add(dto);
        }
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
        return result;
    }
    public int registerNewTimeTable(int planId, TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndDecrement();
        timetable.changeId(tempId);
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timetable.getTimeTableId(), TimeTableDto.fromEntity(timetable));
        List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        timeTableIds.add(timetable.getTimeTableId());
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
        return tempId;
    }

    public void deleteRedisTimeTable(List<Integer> timetableIds) {
        List<String> timetableKeys = timetableIds.stream()
                .map(id -> TIMETABLE_PREFIX + id)
                .toList();
        timeTableRedis.delete(timetableKeys);

        // placeBlock 삭제
        deleteRedisTimeTableBlockByTimeTableId(timetableIds);
    }

    private void deleteRedisTimeTableBlockByTimeTableId(List<Integer> timetableIds) {
        List<Integer> placeBlockIds = timetableIds.stream()
                .map(id -> timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + id))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();

        List<String> placeBlockKeys = placeBlockIds.stream()
                .map(id -> TIMETABLEPLACEBLOCK_PREFIX + id)
                .toList();
        timeTablePlaceBlockRedis.delete(placeBlockKeys);
    }

    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) {
        for(TimetableVO timeTable : timeTableVOs){
            if(timeTable.getTimetableId() != null){
                timeTableRedis.delete(TIMETABLE_PREFIX + timeTable.getTimetableId());
                List<Integer> timeTableIds = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
                timeTableIds.remove(timeTable.getTimetableId());
                planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, timeTableIds);
                List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(timeTable.getTimetableId());
                if(timeTablePlaceBlocks != null) {
                    for(int timeTablePlaceBlockId : timeTablePlaceBlocks){
                        timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
                    }
                }
            }
        }
    }
    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timeTable.getTimeTableId(), TimeTableDto.fromEntity(timeTable));
    }

    public List<TimeTablePlaceBlock> getTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        if(timeTablePlaceBlocks != null) {
            List<String> keys = new ArrayList<>();
            for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
                keys.add(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
            }
            List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
            if (dtos == null) return Collections.emptyList();
            TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
            List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
            for (TimeTablePlaceBlockDto dto : dtos) {
                if (dto != null) {
                    PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                    result.add(dto.toEntity(pcRef, timeTableRef));
                }
            }
            return result;
        }
        return null;
    }

    public List<TimeTablePlaceBlock> deleteTimeTablePlaceBlockByTimeTableId(int timetableId) {
        List<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForValue().getAndDelete(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timetableId);
        if(timeTablePlaceBlocks != null) {
            List<String> keys = new ArrayList<>(timeTablePlaceBlocks.size());
            for(Integer timeTablePlaceBlockId : timeTablePlaceBlocks){
                keys.add(TIMETABLEPLACEBLOCK_PREFIX + timeTablePlaceBlockId);
            }
            List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockRedis.opsForValue().multiGet(keys);
            if (dtos == null) return Collections.emptyList();
            TimeTable timeTableRef = timeTableRepository.getReferenceById(timetableId);
            List<TimeTablePlaceBlock> result = new ArrayList<>(dtos.size());
            for (TimeTablePlaceBlockDto dto : dtos) {
                if (dto != null) {
                    PlaceCategory pcRef = placeCategoryRepository.getReferenceById(dto.placeCategoryId());
                    result.add(dto.toEntity(pcRef, timeTableRef));
                }
            }
            return result;
        }
        return null;
    }

    public TimeTablePlaceBlock getTimeTablePlaceBlock(int blockId) {
        TimeTablePlaceBlockDto cached = timeTablePlaceBlockRedis.opsForValue().get(TIMETABLEPLACEBLOCK_PREFIX + blockId);
        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTablePlaceBlock 정보가 없습니다.");
        }
        PlaceCategory pcRef = placeCategoryRepository.getReferenceById(cached.placeCategoryId());
        TimeTable ttRef = timeTableRepository.getReferenceById(cached.timeTableId());
        return cached.toEntity(pcRef, ttRef);
    }


    public List<TimeTablePlaceBlockDto> registerTimeTablePlaceBlock(int timeTableId) {
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTableId);
        List<Integer> timeTablePlaceBlockIds = new ArrayList<>();
        List<TimeTablePlaceBlockDto> result = new ArrayList<>();
        for(TimeTablePlaceBlock timeTablePlaceBlock : timeTablePlaceBlocks){
            TimeTablePlaceBlockDto dto = TimeTablePlaceBlockDto.fromEntity(timeTablePlaceBlock);
            timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + dto.blockId(), dto);
            timeTablePlaceBlockIds.add(dto.blockId());
            result.add(dto);
        }
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTablePlaceBlockIds);
        return result;
    }

    public int registerNewTimeTablePlaceBlock(int timeTableId, TimeTablePlaceBlock block) {
        int tempId = timeTablePlaceBlockTempIdGenerator.getAndDecrement();
        block.changeId(tempId);
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), TimeTablePlaceBlockDto.fromEntity(block));
        List<Integer> timeTableBlockIds = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        if(timeTableBlockIds==null) timeTableBlockIds = new ArrayList<>();
        timeTableBlockIds.add(block.getBlockId());
        timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTableBlockIds);
        return tempId;
    }

    public void deleteTimeTablePlaceBlock(int timeTableId, int blockId) {
        timeTablePlaceBlockRedis.delete(TIMETABLEPLACEBLOCK_PREFIX +blockId);
        List<Integer> timeTablePlaceBlockIds = timeTableToTimeTablePlaceBlockRedis.opsForValue().get(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId);
        if(timeTablePlaceBlockIds != null) {
            timeTablePlaceBlockIds.remove(Integer.valueOf(blockId));
            timeTableToTimeTablePlaceBlockRedis.opsForValue().set(TIMETABLETOTIMETABLEPLACEBLOCK_PREFIX + timeTableId, timeTablePlaceBlockIds);
        }
    }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) {
        timeTablePlaceBlockRedis.opsForValue().set(TIMETABLEPLACEBLOCK_PREFIX + block.getBlockId(), TimeTablePlaceBlockDto.fromEntity(block));
    }

    public Travel getTravelByTravelId(int travelId) {
        TravelDto dto = travelRedis.opsForValue().get(TRAVEL_PREFIX + travelId);
        if (dto == null) return null;
        return dto.toEntity(travelCategoryRepository.getReferenceById(dto.travelCategoryId()));
    }

    public PlaceCategory getPlaceCategory(int placeCategoryId) {
        PlaceCategoryDto dto = placeCategoryRedis.opsForValue().get(PLACECATEGORY_PREFIX + placeCategoryId);
        if (dto == null) return placeCategoryRepository.getReferenceById(placeCategoryId);
        return dto.toEntity();
    }

    public void registerRefreshToken(String token, int userId) {
        long ttl = 14L; // 14일
        refreshTokenRedis.opsForValue().set(
                REFRESHTOKEN_PREFIX + token,
                userId,
                ttl,
                TimeUnit.DAYS
        );
    }

    public Integer findUserIdByRefreshToken(String refreshToken) {
        return refreshTokenRedis.opsForValue().get(REFRESHTOKEN_PREFIX + refreshToken);
    }
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenRedis.delete(REFRESHTOKEN_PREFIX + refreshToken);
    }
    public String getNicknameByUserId(int userId) { return  userIdNicknameRedis.opsForValue().get(USERID_NICKNAME_PREFIX + userId); }
    public Integer getUserIdByNickname(String nickname){ return nicknameUseridRedis.opsForValue().get(NICKNAME_USERID_PREFIX + nickname); }
    public void registerNickname(int userId, String nickname) {
        userIdNicknameRedis.opsForValue().set(USERID_NICKNAME_PREFIX + userId, nickname);
    }
    public boolean hasPlanTracker(int planId) {
        return planTrackerRedis.hasKey(PLANTRACKER_PREFIX + planId);
    }
    public void registerPlanTracker(int planId, int userId, int dayIndex) {
        planTrackerRedis.opsForHash().put(PLANTRACKER_PREFIX + planId, userId, dayIndex);
    }
    public void registerPlanTracker(int planId, List<UserDayIndexVO> userDayIndexVOs) {
        for(UserDayIndexVO userDayIndexVO : userDayIndexVOs){
            int userId = getUserIdByNickname(userDayIndexVO.getNickname());
            planTrackerRedis.opsForHash().put(PLANTRACKER_PREFIX + planId, userId, userDayIndexVO.getDayIndex());
        }
    }

    public List<UserDayIndexVO> getPlanTracker(int planId) {
        String key = PLANTRACKER_PREFIX + planId;

        Map<Object, Object> entries = planTrackerRedis.opsForHash().entries(key);
        if (entries.isEmpty()) return Collections.emptyList();

        List<UserDayIndexVO> result = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            Integer userId   = (Integer) e.getKey();     // hash field
            Integer dayIndex = (Integer) e.getValue();   // hash value
            String nickname  = getNicknameByUserId(userId);
            result.add(new UserDayIndexVO(nickname, dayIndex));
        }
        return result;
    }
    public void removePlanTracker(int planId, int userId) {
        planTrackerRedis.opsForHash().delete(PLANTRACKER_PREFIX + planId, userId);
    }

    public void registerNickname(int userId) {
        User user = userRepository.findById(userId).get();
        userIdNicknameRedis.opsForValue().set(USERID_NICKNAME_PREFIX + user.getUserId(), user.getNickname());
        nicknameUseridRedis.opsForValue().set(NICKNAME_USERID_PREFIX + user.getNickname(), user.getUserId());
    }
    public void removeNickname(int userId) {
        String nickname = userIdNicknameRedis.opsForValue().getAndDelete(USERID_NICKNAME_PREFIX + userId);
        nicknameUseridRedis.delete(NICKNAME_USERID_PREFIX + nickname);
    }

    public void registerUserIdToPlanId(int planId, int userId){
        userIdToPlanIdRedis.opsForValue().set(USERIDTOPLANID_PREFIX + userId, planId);
    }
    public int getPlanIdByUserId(int userId){
        return userIdToPlanIdRedis.opsForValue().get(USERIDTOPLANID_PREFIX + userId);
    }
    public int removeUserIdToPlanId(int userId){
        return userIdToPlanIdRedis.opsForValue().getAndDelete(USERIDTOPLANID_PREFIX + userId);
    }

}
