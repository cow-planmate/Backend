package com.example.planmate.domain.webSocket.service;

import com.example.planmate.domain.plan.entity.*;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.webSocket.dto.*;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.webSocket.dto.service.RedisService;
import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketPlanService {
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RedisService redisService;
    private final EntityManager entityManager;

    public WPlanResponse updatePlan(int planId, WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        Plan plan = redisService.getPlan(planId);

        if(request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
            response.setPlanName(plan.getPlanName());
        }

        if(request.getTravelId() != null) {
            Travel travel = redisService.getTravelByTravelId(request.getTravelId());
            plan.setTravel(travel);
            response.setTravelId(travel.getTravelId());
            response.setTravelName(travel.getTravelName());
        }
        if(request.getAdultCount() != null) {
            plan.setAdultCount(request.getAdultCount());
            response.setAdultCount(request.getAdultCount());
        }
        if(request.getChildCount() != null) {
            plan.setChildCount(request.getChildCount());
            response.setChildCount(request.getChildCount());
        }
        if(request.getDeparture() != null) {
            plan.setDeparture(request.getDeparture());
            response.setDeparture(request.getDeparture());
        }
        if(request.getTransportationCategoryId() != null) {
            plan.setTransportationCategory(new TransportationCategory(request.getTransportationCategoryId()));
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
            int tempId = redisService.registerNewTimeTable(timeTable);
            timetableVO.setTimetableId(tempId);
            response.addTimetableVO(timetableVO);
        }
        return response;
    }

    public WTimeTablePlaceBlockResponse createTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        TimeTable timetable = timeTableRepository.findById(request.getTimetablePlaceBlockVO().getTimetableId()).orElse(null);
        PlaceCategory placeCategory = placeCategoryRepository.findById(timetablePlaceBlockVO.getPlaceCategoryId()).orElse(null);
        TimeTablePlaceBlock timeTablePlaceBlock = TimeTablePlaceBlock.builder()
                .timeTable(timetable)
                .placeName(timetablePlaceBlockVO.getPlaceName())
                .placeTheme(timetablePlaceBlockVO.getPlaceTheme())
                .placeRating(timetablePlaceBlockVO.getPlaceRating())
                .placeAddress(timetablePlaceBlockVO.getPlaceAddress())
                .placeLink(timetablePlaceBlockVO.getPlaceLink())
                .blockStartTime(timetablePlaceBlockVO.getStartTime())
                .blockEndTime(timetablePlaceBlockVO.getEndTime())
                .xLocation(timetablePlaceBlockVO.getXLocation())
                .yLocation(timetablePlaceBlockVO.getYLocation())
                .placeCategory(placeCategory)
                .build();
        timeTablePlaceBlockRepository.save(timeTablePlaceBlock);
        timetablePlaceBlockVO.setTimetablePlaceBlockId(timeTablePlaceBlock.getBlockId());
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
            timetable.setTimeTableEndTime(timetableVO.getEndTime());
            timetable.setTimeTableStartTime(timetableVO.getStartTime());
            redisService.updateTimeTable(timetable);
            response.addTimetableVO(timetableVO);
        }
        return response;
    }

    public WTimeTablePlaceBlockResponse updateTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        TimeTablePlaceBlock timetablePlaceBlock = redisService.getTimeTablePlaceBlock(timetablePlaceBlockVO.getTimetablePlaceBlockId());
        if (timetablePlaceBlockVO.getPlaceName() != null) {
            timetablePlaceBlock.setPlaceName(timetablePlaceBlockVO.getPlaceName());
        }
        if (timetablePlaceBlockVO.getPlaceTheme() != null) {
            timetablePlaceBlock.setPlaceTheme(timetablePlaceBlockVO.getPlaceTheme());
        }
        if (timetablePlaceBlockVO.getPlaceRating() != null) {
            timetablePlaceBlock.setPlaceRating(timetablePlaceBlockVO.getPlaceRating());
        }
        if (timetablePlaceBlockVO.getPlaceAddress() != null) {
            timetablePlaceBlock.setPlaceAddress(timetablePlaceBlockVO.getPlaceAddress());
        }
        if (timetablePlaceBlockVO.getPlaceLink() != null) {
            timetablePlaceBlock.setPlaceLink(timetablePlaceBlockVO.getPlaceLink());
        }
        if (timetablePlaceBlockVO.getStartTime() != null) {
            timetablePlaceBlock.setBlockStartTime(timetablePlaceBlockVO.getStartTime());
        }
        if (timetablePlaceBlockVO.getEndTime() != null) {
            timetablePlaceBlock.setBlockEndTime(timetablePlaceBlockVO.getEndTime());
        }
        if (timetablePlaceBlockVO.getXLocation() != null) {
            timetablePlaceBlock.setXLocation(timetablePlaceBlockVO.getXLocation());
        }
        if (timetablePlaceBlockVO.getYLocation() != null) {
            timetablePlaceBlock.setYLocation(timetablePlaceBlockVO.getYLocation());
        }
        if (timetablePlaceBlockVO.getPlaceCategoryId() != null) {
            timetablePlaceBlock.setPlaceCategory(
                    entityManager.getReference(PlaceCategory.class, timetablePlaceBlockVO.getPlaceCategoryId())
            );
        }
        redisService.updateTimeTablePlaceBlock(timetablePlaceBlock);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimetableResponse deleteTimetable(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        for(TimetableVO timetableVO : request.getTimetableVOs()) {
            response.addTimetableVO(timetableVO);
            redisService.deleteTimeTable(timetableVO.getTimetableId());
        }
        return response;
    }
    public WTimeTablePlaceBlockResponse deleteTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        redisService.deleteTimeTablePlaceBlock(request.getTimetablePlaceBlockVO().getTimetableId());
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        response.setTimetablePlaceBlockVO(request.getTimetablePlaceBlockVO());
        return response;
    }

}
