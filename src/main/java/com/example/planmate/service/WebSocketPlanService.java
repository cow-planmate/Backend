package com.example.planmate.service;

import com.example.planmate.entity.*;
import com.example.planmate.repository.PlaceCategoryRepository;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.valueObject.TimetableVO;
import com.example.planmate.wdto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class WebSocketPlanService {
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final RedisService redisService;
    private final EntityManager entityManager;

    public WResponse run(int planId, WRequest request) {

        return switch (request.getType().toLowerCase()) {
            case "create" -> createObject(planId, request);
            case "update" -> updateObject(planId, request);
            case "delete" -> deleteObject(planId, request);
            default -> throw new IllegalArgumentException("Unknown type: " + request.getType());
        };
    }

    private WResponse createObject(int planId, WRequest request) {
        return switch (request.getObject()){
            case "timetable" -> createTimetable(planId, (WTimetableRequest) request);
            case "timetablePlaceBlock" -> createTimetablePlaceBlock((WTimeTablePlaceBlockRequest)request);
            default -> throw new IllegalArgumentException("Unknown object: " + request.getType() + request.getObject());
        };
    }
    private WResponse createTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        TimetableVO timetableVO = request.getTimetableVO();

        Plan plan = redisService.getPlan(planId);
        TimeTable timeTable = TimeTable.builder()
                .plan(plan)
                .date(timetableVO.getDate())
                .timeTableStartTime(timetableVO.getStartTime())
                .timeTableEndTime(timetableVO.getEndTime())
                .build();

        int tempId = redisService.registerNewTimeTable(timeTable);
        timetableVO.setTimetableId(tempId);
        response.setTimetableVO(timetableVO);
        return response;
    }

    private WResponse createTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
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
                .blockStartTime(timetablePlaceBlockVO.getBlockStartTime())
                .blockEndTime(timetablePlaceBlockVO.getBlockEndTime())
                .xLocation(timetablePlaceBlockVO.getXLocation())
                .yLocation(timetablePlaceBlockVO.getYLocation())
                .placeCategory(placeCategory)
                .build();
        timeTablePlaceBlockRepository.save(timeTablePlaceBlock);
        timetablePlaceBlockVO.setTimetablePlaceBlockId(timeTablePlaceBlock.getBlockId());
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    private WResponse updateObject(int planId, WRequest request) {
        Plan plan = planRepository.findById(planId).orElse(null);
        return switch (request.getObject()){
            case "plan" -> updatePlan(plan, (WPlanRequest)request);
            case "timetable" -> updateTimetable(plan, (WTimetableRequest)request);
            case "timetablePlaceBlock" -> updateTimetablePlaceBlock((WTimeTablePlaceBlockRequest)request);
            default -> throw new IllegalArgumentException("Unknown object: " + request.getType() + request.getObject());
        };
    }
    private WResponse updatePlan(Plan plan, WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        if(request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        if(request.getTravelId() != null) {
            plan.setTravel(entityManager.getReference(Travel.class, request.getTravelId()));
        }
        if(request.getAdultCount() != null) {
            plan.setAdultCount(request.getAdultCount());
        }
        if(request.getChildCount() != null) {
            plan.setChildCount(request.getChildCount());
        }
        if(request.getDeparture() != null) {
            plan.setDeparture(request.getDeparture());
        }
        if(request.getTransportationCategoryId() != null) {
            plan.setTransportationCategory(entityManager.getReference(TransportationCategory.class, request.getTransportationCategoryId()));
        }
        redisService.updatePlan(plan);
        planRepository.save(plan);
        response.setPlanName(request.getPlanName());
        return response;
    }

    private WResponse updateTimetable(Plan plan, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        TimetableVO timetableVO = request.getTimetableVO();
        int timetableId = timetableVO.getTimetableId();
        TimeTable timetable = redisService.getTimeTable(timetableId);
        if(timetable.getPlan().getPlanId() != plan.getPlanId()) {
            throw new AccessDeniedException("timetable 접근 권한이 없습니다");
        }
        timetable.setTimeTableEndTime(timetableVO.getEndTime());
        timetable.setTimeTableStartTime(timetableVO.getStartTime());
        redisService.updateTimeTable(timetable);
        response.setTimetableVO(timetableVO);
        return response;
    }

    private WResponse updateTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
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
        if (timetablePlaceBlockVO.getBlockStartTime() != null) {
            timetablePlaceBlock.setBlockStartTime(timetablePlaceBlockVO.getBlockStartTime());
        }
        if (timetablePlaceBlockVO.getBlockEndTime() != null) {
            timetablePlaceBlock.setBlockEndTime(timetablePlaceBlockVO.getBlockEndTime());
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

    private WResponse deleteObject(int planId, WRequest request) {
        return switch (request.getObject()){
            case "timetable" -> deleteTimetable((WTimetableRequest)request);
            case "timetablePlaceBlock" -> deleteTimetablePlaceBlock((WTimeTablePlaceBlockRequest)request);
            default -> throw new IllegalArgumentException("Unknown object: " + request.getType() + request.getObject());
        };
    }
    private WTimetableResponse deleteTimetable(WTimetableRequest request) {
        redisService.deleteTimeTable(request.getTimetableVO().getTimetableId());
        WTimetableResponse response = new WTimetableResponse();
        response.setTimetableVO(request.getTimetableVO());
        return response;
    }
    private WTimeTablePlaceBlockResponse deleteTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        redisService.deleteTimeTablePlaceBlock(request.getTimetablePlaceBlockVO().getTimetableId());
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        response.setTimetablePlaceBlockVO(request.getTimetablePlaceBlockVO());
        return response;
    }


}
