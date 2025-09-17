package com.example.planmate.domain.webSocket.service;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.webSocket.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketPlanService {
    private final RedisService redisService;
    private final PlacePhotoRepository placePhotoRepository;

    public WPlanResponse updatePlan(int planId, WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        Plan plan = redisService.getPlan(planId);

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
        redisService.updatePlan(plan);
        return response;
    }

    public WTimetableResponse createTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();

        Plan plan = redisService.getPlan(planId);
        for(TimetableVO timetableVO : timetableVOs) {
            TimeTable timeTable = TimeTable.builder()
                    .plan(plan)
                    .date(timetableVO.getDate())
                    .timeTableStartTime(timetableVO.getStartTime())
                    .timeTableEndTime(timetableVO.getEndTime())
                    .build();
            int tempId = redisService.registerNewTimeTable(planId, timeTable);
            timetableVO.setTimetableId(tempId);
            response.addTimetableVO(timetableVO);
        }

        return response;
    }

    public WTimeTablePlaceBlockResponse createTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        TimeTablePlaceBlock timeTablePlaceBlock = TimeTablePlaceBlock.builder()
                .timeTable(redisService.getTimeTable(timetablePlaceBlockVO.getTimetableId()))
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
        int tempId = redisService.registerNewTimeTablePlaceBlock(timetablePlaceBlockVO.getTimetableId(), timeTablePlaceBlock);
        timetablePlaceBlockVO.setTimetablePlaceBlockId(tempId);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimetableResponse updateTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();
        for(TimetableVO timetableVO : timetableVOs) {
            int timetableId = timetableVO.getTimetableId();
            TimeTable timetable = redisService.getTimeTable(timetableId);
            if(timetable.getPlan().getPlanId() != planId) {
                throw new AccessDeniedException("timetable 접근 권한이 없습니다");
            }
            timetable.changeDate(timetableVO.getDate());
            timetable.changeTime(timetableVO.getStartTime(), timetableVO.getEndTime());
            redisService.updateTimeTable(timetable);
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
        TimeTablePlaceBlock timetablePlaceBlock = redisService.getTimeTablePlaceBlock(timetablePlaceBlockVO.getTimetablePlaceBlockId());
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
        redisService.updateTimeTablePlaceBlock(timetablePlaceBlock);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimetableResponse deleteTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        redisService.deleteTimeTable(planId, request.getTimetableVOs());
        response.setTimetableVOs(request.getTimetableVOs());
        return response;
    }
    public WTimeTablePlaceBlockResponse deleteTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        if(request.getTimetablePlaceBlockVO()!=null) {
            redisService.deleteTimeTablePlaceBlock(request.getTimetablePlaceBlockVO().getTimetableId(), request.getTimetablePlaceBlockVO().getTimetablePlaceBlockId());
            response.setTimetablePlaceBlockVO(request.getTimetablePlaceBlockVO());
        }
        return response;
    }

    public WPresencesResponse updatePresence(int planId, WPresencesRequest request) {
        WPresencesResponse response = new WPresencesResponse();
        redisService.registerPlanTracker(planId, request.getUserDayIndexVO());
        response.setUserDayIndexVOs(request.getUserDayIndexVO());
        return response;
    }
}
