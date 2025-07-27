package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.WebSocketPlanRequest;
import com.example.planmate.dto.WebSocketPlanResponse;
import com.example.planmate.entity.PlaceCategory;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.externalAPI.GoogleMap;
import com.example.planmate.repository.*;
import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.valueObject.TimetableVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WebSocketPlanService {
    private final PlanRepository planRepository;
    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelRepository travelRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final GoogleMap googleMap;
    private final CacheService cacheService;

    public WebSocketPlanResponse run(int planId, WebSocketPlanRequest request) {

        return switch (request.getType().toLowerCase()) {
            case "create" -> createPlan(planId, request);
            case "update" -> updatePlan(planId, request);
            case "delete" -> deletePlan(planId, request);
            default -> throw new IllegalArgumentException("Unknown type: " + request.getType());
        };
    }

    private WebSocketPlanResponse createPlan(int planId, WebSocketPlanRequest request) {
        return switch (request.getObject()){
            case "timetable" -> createTimetable(planId, request);
            case "timetablePlaceBlock" -> createTimetablePlaceBlock(request);
            default -> throw new IllegalArgumentException("Unknown object: " + request.getType() + request.getObject());
        };
    }
    private WebSocketPlanResponse createTimetable(int planId, WebSocketPlanRequest request) {
        WebSocketPlanResponse response = new WebSocketPlanResponse();
        TimetableVO timetableVO = request.getTimetableVO();

        Plan plan = cacheService.getPlan(planId);
        TimeTable timeTable = TimeTable.builder()
                .plan(plan)
                .date(timetableVO.getDate())
                .timeTableStartTime(timetableVO.getStartTime())
                .timeTableEndTime(timetableVO.getEndTime())
                .build();

        int tempId = cacheService.registerTimeTable(timeTable);
        timetableVO.setTimetableId(tempId);
        response.setTimetableVO(timetableVO);
        return response;
    }

    private WebSocketPlanResponse createTimetablePlaceBlock(WebSocketPlanRequest request) {
        WebSocketPlanResponse response = new WebSocketPlanResponse();
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

    private WebSocketPlanResponse updatePlan(int planId, WebSocketPlanRequest request) {
        Plan plan = planRepository.findById(planId).orElse(null);
        return switch (request.getObject()){
            case "planName" -> updatePlanName(plan, request);
            case "travel" -> updateTravel(plan, request);
            case "departure" -> updateDeparture(plan, request);
            case "transportationCategory" -> updateTransportationCategory(plan, request);
            case "adultCount" -> updateAdultCount(plan, request);
            case "childCount" -> updateChildCount(plan, request);
            case "timetable" -> updateTimetable(plan, request);
            case "timetablePlaceBlock" -> updateTimetablePlaceBlock(plan, request);
            default -> throw new IllegalArgumentException("Unknown object: " + request.getType() + request.getObject());
        };
    }
    private WebSocketPlanResponse updatePlanName(Plan plan, WebSocketPlanRequest request) {
        WebSocketPlanResponse response = new WebSocketPlanResponse();
        plan.setPlanName(request.getPlanName());
        planRepository.save(plan);
        response.setPlanName(request.getPlanName());
        return response;
    }

    private WebSocketPlanResponse updateTravel(Plan plan, WebSocketPlanRequest request) {
        WebSocketPlanResponse response = new WebSocketPlanResponse();

    }

    private WebSocketPlanResponse updateDeparture(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse updateTransportationCategory(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse updateAdultCount(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse updateChildCount(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse updateTimetable(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse updateTimetablePlaceBlock(Plan plan, WebSocketPlanRequest request) {
    }

    private WebSocketPlanResponse deletePlan(int planId, WebSocketPlanRequest request) {
    }

    @Transactional
    public WebSocketPlanResponse savePlan(int userId, int planId, String departure, Integer transportationCategoryId, Integer adultCount, Integer childCount, TimetableVO timetableVO, TimetablePlaceBlockVO timetablePlaceBlock) {
        WebSocketPlanResponse response = new WebSocketPlanResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);

        if(transportationCategoryId!=null){
            plan.setTransportationCategory(transportationCategoryRepository.findById(transportationCategoryId).get());
        }
        if(departure!=null){
            plan.setDeparture(departure);
            response.setDeparture(departure);
        }
        if(adultCount!=null){
            plan.setAdultCount(adultCount);
            response.setAdultCount(adultCount);
        }
        if(childCount!=null){
            plan.setChildCount(childCount);
            response.setChildCount(childCount);
        }
        if(timetableVO!=null){
            List<TimeTable> timeTables = changeTimetable(plan, timetableVO);

        }


        changeTimetablePlaceBlock(plan, timetablePlaceBlockLists, timeTables);

        try {
            planRepository.save(plan);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("입력 데이터가 유효하지 않습니다.", ex);
        }
        return response;
    }
    private TimeTable changeTimetable(Plan plan, TimetableVO timetableVO) {
        if(timetableVO.getTimetableId()==null){
            TimeTable timeTable = TimeTable.builder()
                    .date(timetableVO.getDate())
                    .timeTableStartTime(timetableVO.getStartTime())
                    .timeTableEndTime(timetableVO.getEndTime())
                    .build();
        }
        else{
            TimeTable timeTable = timeTableRepository.findById(timetableVO.getTimetableId()).get();
            if(timetableVO.getDate()!=null){
                timeTable.setDate(timetableVO.getDate());
            }
            else if(timetableVO.getStartTime()!=null){
                timeTable.setTimeTableStartTime(timetableVO.getStartTime());
            }
            else if(timetableVO.getEndTime()!=null){
                timeTable.setTimeTableEndTime(timetableVO.getEndTime());
            }
        }
        return timeTable;
    }
    private void changeTimetablePlaceBlock(Plan plan, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockLists, List<TimeTable> timeTables) {
        if(timetablePlaceBlockLists == null || timetablePlaceBlockLists.isEmpty()) {
            return;
        }
        timeTablePlaceBlockRepository.deleteAllByTimeTable_Plan(plan);
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = new ArrayList<>();
        for(int i = 0; i < timetablePlaceBlockLists.size(); i++){
            if(timeTables.isEmpty()){
                break;
            }
            TimeTable timetable = timeTables.get(i);
            for(int j = 0; j < timetablePlaceBlockLists.get(i).size(); j++){
                TimetablePlaceBlockVO timeTablePlaceBlockVO = timetablePlaceBlockLists.get(i).get(j);
                PlaceCategory placeCategory = placeCategoryRepository.getReferenceById(timeTablePlaceBlockVO.getPlaceCategoryId());
                timeTablePlaceBlocks.add(TimeTablePlaceBlock.builder()
                        .timeTable(timetable)
                        .placeName(timeTablePlaceBlockVO.getPlaceName())
                        .placeTheme(timeTablePlaceBlockVO.getPlaceTheme())
                        .placeRating(timeTablePlaceBlockVO.getPlaceRating())
                        .placeAddress(timeTablePlaceBlockVO.getPlaceAddress())
                        .placeLink(timeTablePlaceBlockVO.getPlaceLink())
                        .blockStartTime(timeTablePlaceBlockVO.getStartTime())
                        .blockEndTime(timeTablePlaceBlockVO.getEndTime())
                        .xLocation(timeTablePlaceBlockVO.getXLocation())
                        .yLocation(timeTablePlaceBlockVO.getYLocation())
                        .placeCategory(placeCategory)
                        .build());
            }
        }
        timeTablePlaceBlockRepository.saveAll(timeTablePlaceBlocks);
    }


}
