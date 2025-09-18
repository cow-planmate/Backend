package com.example.planmate.domain.webSocket.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.webSocket.dto.WPlanRequest;
import com.example.planmate.domain.webSocket.dto.WPlanResponse;
import com.example.planmate.domain.webSocket.dto.WPresencesRequest;
import com.example.planmate.domain.webSocket.dto.WPresencesResponse;
import com.example.planmate.domain.webSocket.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.webSocket.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.domain.webSocket.dto.WTimetableRequest;
import com.example.planmate.domain.webSocket.dto.WTimetableResponse;
import com.example.planmate.infrastructure.redis.PlanCacheService;
import com.example.planmate.infrastructure.redis.PlanTrackerService;
import com.example.planmate.infrastructure.redis.TimeTableCacheService;
import com.example.planmate.infrastructure.redis.TimeTablePlaceBlockCacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebSocketPlanService {
    private final PlanCacheService planCacheService;
    private final TimeTableCacheService timeTableCacheService;
    private final TimeTablePlaceBlockCacheService blockCacheService;
    private final PlanTrackerService planTrackerService;
    private final PlacePhotoRepository placePhotoRepository;
    private final RedisService redisService; // only for travel & placeCategory until moved

    public WPlanResponse updatePlan(int planId, WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        Plan plan = planCacheService.get(planId);
        if(plan == null){
            plan = planCacheService.loadPlan(planId);
        }

        if(request.getPlanName() != null) {
            plan.changePlanName(request.getPlanName());
            response.setPlanName(plan.getPlanName());
        }

        if(request.getTravelId() != null) {
            Travel travel = redisService.getTravelByTravelId(request.getTravelId());
            plan.changeTravel(travel);
            response.setTravelId(travel.getTravelId());
            response.setTravelName(travel.getTravelName());
        }
        if (request.getAdultCount() != null || request.getChildCount() != null) {
            int adult = request.getAdultCount() != null ? request.getAdultCount() : plan.getAdultCount();
            int child = request.getChildCount() != null ? request.getChildCount() : plan.getChildCount();
            plan.updateCounts(adult, child);

            response.setAdultCount(adult);
            response.setChildCount(child);
        }
        if(request.getDeparture() != null) {
            plan.changeDeparture(request.getDeparture());
            response.setDeparture(request.getDeparture());
        }
        if(request.getTransportationCategoryId() != null) {
            plan.changeTransportationCategory(new TransportationCategory(request.getTransportationCategoryId()));
            response.setTransportationCategoryId(request.getTransportationCategoryId());
        }
        planCacheService.update(plan);
        return response;
    }

    public WTimetableResponse createTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();

        Plan plan = planCacheService.get(planId);
        if(plan == null){
            plan = planCacheService.loadPlan(planId);
            timeTableCacheService.loadForPlan(planId);
        }
        for(TimetableVO timetableVO : timetableVOs) {
            TimeTable timeTable = TimeTable.builder()
                    .plan(plan)
                    .date(timetableVO.getDate())
                    .timeTableStartTime(timetableVO.getStartTime())
                    .timeTableEndTime(timetableVO.getEndTime())
                    .build();
            int tempId = timeTableCacheService.addNew(planId, timeTable);
            timetableVO.setTimetableId(tempId);
            response.addTimetableVO(timetableVO);
        }

        return response;
    }

    public WTimeTablePlaceBlockResponse createTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        TimeTable timetable = timeTableCacheService.get(timetablePlaceBlockVO.getTimetableId());
        if(timetable == null){
            throw new IllegalStateException("Timetable not found in cache");
        }
        TimeTablePlaceBlock timeTablePlaceBlock = TimeTablePlaceBlock.builder()
                .timeTable(timetable)
                .placeName(timetablePlaceBlockVO.getPlaceName())
                .placeTheme("")
                .placeRating(timetablePlaceBlockVO.getPlaceRating())
                .placeAddress(timetablePlaceBlockVO.getPlaceAddress())
                .placeLink(timetablePlaceBlockVO.getPlaceLink())
                .placePhoto(placePhotoRepository.getReferenceById(timetablePlaceBlockVO.getPlaceId()))
                .blockStartTime(timetablePlaceBlockVO.getStartTime())
                .blockEndTime(timetablePlaceBlockVO.getEndTime())
                .xLocation(timetablePlaceBlockVO.getXLocation())
                .yLocation(timetablePlaceBlockVO.getYLocation())
                .placeCategory(redisService.getPlaceCategory(timetablePlaceBlockVO.getPlaceCategoryId()))
                .build();
        int tempId = blockCacheService.addNew(timetablePlaceBlockVO.getTimetableId(), timeTablePlaceBlock);
        timetablePlaceBlockVO.setTimetablePlaceBlockId(tempId);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimetableResponse updateTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();
        for(TimetableVO timetableVO : timetableVOs) {
            int timetableId = timetableVO.getTimetableId();
            TimeTable timetable = timeTableCacheService.get(timetableId);
            if(timetable == null){
                throw new IllegalStateException("Timetable not found in cache");
            }
            if(timetable.getPlan().getPlanId() != planId) {
                throw new AccessDeniedException("timetable 접근 권한이 없습니다");
            }
            timetable.changeDate(timetableVO.getDate());
            timetable.changeTime(timetableVO.getStartTime(), timetableVO.getEndTime());
            timeTableCacheService.update(timetable);
            response.addTimetableVO(timetableVO);
        }
        response.sortTimetableVOs();
        return response;
    }

    public WTimeTablePlaceBlockResponse updateTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        if(timetablePlaceBlockVO.getTimetablePlaceBlockId() == null) {
            return response;
        }
        // For update we need the existing block; currently only cached after creation, so fetch list and find
        List<TimeTablePlaceBlock> blocks = blockCacheService.getByTimeTable(timetablePlaceBlockVO.getTimetableId());
        TimeTablePlaceBlock timetablePlaceBlock = blocks.stream()
                .filter(b -> b.getBlockId().equals(timetablePlaceBlockVO.getTimetablePlaceBlockId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Block not found in cache"));
        if (timetablePlaceBlockVO.getPlaceName() != null) {
            timetablePlaceBlock.changePlaceName(timetablePlaceBlockVO.getPlaceName());
        }

        if (timetablePlaceBlockVO.getPlaceRating() != null) {
            timetablePlaceBlock.changeRating(timetablePlaceBlockVO.getPlaceRating());
        }
        if (timetablePlaceBlockVO.getPlaceAddress() != null) {
            timetablePlaceBlock.changeAddress(timetablePlaceBlockVO.getPlaceAddress());
        }
        if (timetablePlaceBlockVO.getPlaceLink() != null) {
            timetablePlaceBlock.changeLink(timetablePlaceBlockVO.getPlaceLink());
        }
        if (timetablePlaceBlockVO.getStartTime() != null || timetablePlaceBlockVO.getEndTime() != null) {
            timetablePlaceBlock.changeTimes(
                    timetablePlaceBlockVO.getStartTime() != null ? timetablePlaceBlockVO.getStartTime() : timetablePlaceBlock.getBlockStartTime(),
                    timetablePlaceBlockVO.getEndTime() != null ? timetablePlaceBlockVO.getEndTime() : timetablePlaceBlock.getBlockEndTime()
            );
        }
        if (timetablePlaceBlockVO.getXLocation() != null || timetablePlaceBlockVO.getYLocation() != null) {
            timetablePlaceBlock.changeLocation(
                    timetablePlaceBlockVO.getXLocation() != null ? timetablePlaceBlockVO.getXLocation() : timetablePlaceBlock.getXLocation(),
                    timetablePlaceBlockVO.getYLocation() != null ? timetablePlaceBlockVO.getYLocation() : timetablePlaceBlock.getYLocation()
            );
        }
        if (timetablePlaceBlockVO.getPlaceCategoryId() != null) {
            timetablePlaceBlock.changeCategory(redisService.getPlaceCategory(timetablePlaceBlockVO.getPlaceCategoryId()));
        }
        blockCacheService.update(timetablePlaceBlock);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimetableResponse deleteTimetable(int planId, WTimetableRequest request) {
        // TODO: implement deletion in new cache services (not yet supported)
        WTimetableResponse response = new WTimetableResponse();
        response.setTimetableVOs(request.getTimetableVOs());
        return response;
    }
    public WTimeTablePlaceBlockResponse deleteTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        // TODO: implement deletion in new cache services (not yet supported)
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        if(request.getTimetablePlaceBlockVO()!=null) {
            response.setTimetablePlaceBlockVO(request.getTimetablePlaceBlockVO());
        }
        return response;
    }

    public WPresencesResponse updatePresence(int planId, WPresencesRequest request) {
        WPresencesResponse response = new WPresencesResponse();
        planTrackerService.registerBulk(planId, request.getUserDayIndexVO());
        response.setUserDayIndexVOs(request.getUserDayIndexVO());
        return response;
    }
}
