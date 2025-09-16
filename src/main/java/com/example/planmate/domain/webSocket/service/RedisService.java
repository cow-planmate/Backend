package com.example.planmate.domain.webSocket.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.redis.cache.PlaceCategoryCacheService;
import com.example.planmate.domain.redis.cache.PlanCacheService;
import com.example.planmate.domain.redis.cache.TimeTableCacheService;
import com.example.planmate.domain.redis.cache.TimeTablePlaceBlockCacheService;
import com.example.planmate.domain.redis.cache.TravelCacheService;
import com.example.planmate.domain.redis.service.NicknameIndexService;
import com.example.planmate.domain.redis.service.PlanTrackerService;
import com.example.planmate.domain.redis.service.RefreshTokenStore;
import com.example.planmate.domain.redis.service.UserPlanIndexService;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;
import com.example.planmate.domain.webSocket.lazydto.TimeTablePlaceBlockDto;
import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {
    
    private final AtomicInteger timeTablePlaceBlockTempIdGenerator = new AtomicInteger(-1);
    private final UserRepository userRepository;
    
    // Delegated services (extracted)
    private final NicknameIndexService nicknameIndexService;
    private final UserPlanIndexService userPlanIndexService;
    private final PlanTrackerService planTrackerService;
    private final RefreshTokenStore refreshTokenStore;
    private final TravelCacheService travelCacheService;
    private final PlaceCategoryCacheService placeCategoryCacheService;
    private final PlanCacheService planCacheService;
    private final TimeTableCacheService timeTableCacheService;
    private final TimeTablePlaceBlockCacheService timeTablePlaceBlockCacheService;

    // Warmup moved to CacheWarmupRunner


    public void registerPlan(int planId){
        planCacheService.registerPlan(planId);
    }
    public Plan getPlan(int planId) { return planCacheService.getPlan(planId); }
    public void updatePlan(Plan plan) { planCacheService.updatePlan(plan); }
    public void deletePlan(int planId) { planCacheService.deletePlan(planId); }

    public TimeTable getTimeTable(int timetableId) { return timeTableCacheService.getTimeTable(timetableId); }

    public List<TimeTable> getTimeTableByPlanId(int planId) { return timeTableCacheService.getTimeTableByPlanId(planId); }

    public List<TimeTable> deleteTimeTableByPlanId(int planId) { return timeTableCacheService.deleteTimeTableByPlanId(planId); }

    public List<TimeTableDto> registerTimeTable(int planId) { return timeTableCacheService.registerTimeTable(planId); }
    public int registerNewTimeTable(int planId, TimeTable timetable) { return timeTableCacheService.registerNewTimeTable(planId, timetable); }

    public void deleteRedisTimeTable(List<Integer> timetableIds) { timeTableCacheService.deleteRedisTimeTable(timetableIds); }

    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) { timeTableCacheService.deleteTimeTable(planId, timeTableVOs); }
    public void updateTimeTable(TimeTable timeTable) {
    timeTableCacheService.updateTimeTable(timeTable);
    }

    public List<TimeTablePlaceBlock> getTimeTablePlaceBlockByTimeTableId(int timetableId) { return timeTablePlaceBlockCacheService.getByTimeTableId(timetableId); }

    public List<TimeTablePlaceBlock> deleteTimeTablePlaceBlockByTimeTableId(int timetableId) { return timeTablePlaceBlockCacheService.deleteByTimeTableId(timetableId); }

    public TimeTablePlaceBlock getTimeTablePlaceBlock(int blockId) { return timeTablePlaceBlockCacheService.get(blockId); }


    public List<TimeTablePlaceBlockDto> registerTimeTablePlaceBlock(int timeTableId) { return timeTablePlaceBlockCacheService.register(timeTableId); }

    public int registerNewTimeTablePlaceBlock(int timeTableId, TimeTablePlaceBlock block) { return timeTablePlaceBlockCacheService.registerNew(timeTableId, block, timeTablePlaceBlockTempIdGenerator.getAndDecrement()); }

    public void deleteTimeTablePlaceBlock(int timeTableId, int blockId) { timeTablePlaceBlockCacheService.delete(timeTableId, blockId); }

    public void updateTimeTablePlaceBlock(TimeTablePlaceBlock block) { timeTablePlaceBlockCacheService.update(block); }

    public Travel getTravelByTravelId(int travelId) {
        return travelCacheService.getTravelByTravelId(travelId);
    }

    public PlaceCategory getPlaceCategory(int placeCategoryId) {
        return placeCategoryCacheService.getPlaceCategory(placeCategoryId);
    }

    public void registerRefreshToken(String token, int userId) {
        refreshTokenStore.registerRefreshToken(token, userId);
    }

    public Integer findUserIdByRefreshToken(String refreshToken) {
        return refreshTokenStore.findUserIdByRefreshToken(refreshToken);
    }
    public void deleteRefreshToken(String refreshToken) {
        refreshTokenStore.deleteRefreshToken(refreshToken);
    }
    public String getNicknameByUserId(int userId) { return nicknameIndexService.getNicknameByUserId(userId); }
    public Integer getUserIdByNickname(String nickname){ return nicknameIndexService.getUserIdByNickname(nickname); }
    public void registerNickname(int userId, String nickname) {
        nicknameIndexService.registerNickname(userId, nickname);
    }
    public boolean hasPlanTracker(int planId) {
        return planTrackerService.hasPlanTracker(planId);
    }
    public void registerPlanTracker(int planId, int userId, int dayIndex) {
        planTrackerService.registerPlanTracker(planId, userId, dayIndex);
    }
    public void registerPlanTracker(int planId, List<UserDayIndexVO> userDayIndexVOs) {
        for(UserDayIndexVO userDayIndexVO : userDayIndexVOs){
            int userId = getUserIdByNickname(userDayIndexVO.getNickname());
            planTrackerService.registerPlanTracker(planId, userId, userDayIndexVO.getDayIndex());
        }
    }

    public List<UserDayIndexVO> getPlanTracker(int planId) {
        return planTrackerService.getPlanTracker(planId);
    }
    public void removePlanTracker(int planId, int userId) {
        planTrackerService.removePlanTracker(planId, userId);
    }

    public void registerNickname(int userId) {
        User user = userRepository.findById(userId).get();
        nicknameIndexService.registerNickname(user.getUserId(), user.getNickname());
    }
    public void removeNickname(int userId) {
        nicknameIndexService.removeNickname(userId);
    }

    public void registerUserIdToPlanId(int planId, int userId){
        userPlanIndexService.registerUserIdToPlanId(planId, userId);
    }
    public int getPlanIdByUserId(int userId){
        Integer planId = userPlanIndexService.getPlanIdByUserId(userId);
        return planId == null ? 0 : planId;
    }
    public int removeUserIdToPlanId(int userId){
        Integer planId = userPlanIndexService.removeUserIdToPlanId(userId);
        return planId == null ? 0 : planId;
    }

}
