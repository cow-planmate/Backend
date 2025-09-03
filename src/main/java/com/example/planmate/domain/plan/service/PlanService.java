package com.example.planmate.domain.plan.service;

import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.dto.*;
import com.example.planmate.domain.plan.entity.*;
import com.example.planmate.domain.plan.repository.*;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.PreferredTheme;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TravelRepository travelRepository;
    private final PlaceCategoryRepository placeCategoryRepository;
    private final PlanEditorRepository planEditorRepository;
    private final RedisService redisService;
    private final GoogleMap googleMap;


    public MakePlanResponse makeService(int userId, String departure, int travelId, int transportationCategoryId, List<LocalDate> dates, int adultCount, int childCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));

        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행지입니다"));

        TransportationCategory transportationCategory = transportationCategoryRepository.findById(transportationCategoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교통수단입니다"));
        Plan plan = Plan.builder()
                .planName(makePlanName(travel))
                .departure(departure)
                .adultCount(adultCount)
                .childCount(childCount)
                .user(user)
                .travel(travel)
                .transportationCategory(transportationCategory)
                .build();
        Plan savedPlan = planRepository.save(plan);
        for (LocalDate date : dates) {
            TimeTable timeTable = TimeTable.builder()
                    .date(date)
                    .timeTableStartTime(LocalTime.of(9, 0))
                    .timeTableEndTime(LocalTime.of(20, 0))
                    .plan(plan)
                    .build();
            timeTableRepository.save(timeTable);
        }
        MakePlanResponse makePlanResponse = new MakePlanResponse();
        makePlanResponse.setPlanId(savedPlan.getPlanId());
        return makePlanResponse;
    }
    public String makePlanName(Travel travel){
        List<Plan> plans = planRepository.findAll();
        List<Integer> index = new ArrayList<>();
        String travelName = travel.getTravelName();
        for (Plan plan : plans) {
            if(plan.getPlanName().contains(travelName)){
                index.add(Integer.parseInt(plan.getPlanName().substring(travelName.length()+1)));
            }
        }
        Collections.sort(index);

        int i = 1;
        for(Integer index2 : index){
            if(i!=index2){break;}
            i++;
        }
        return travel.getTravelName()+ " " + i;
    }

    public GetPlanResponse getPlan(int userId, int planId) {
        GetPlanResponse response = new GetPlanResponse();
        Plan plan = redisService.getPlan(planId);
        List<TimeTable> timeTables;
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();
        if(plan != null) {
            timeTables = redisService.getTimeTableByPlanId(planId);
            for(TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(redisService.getTimeTablePlaceBlockByTimeTableId(timeTable.getTimeTableId()));
            }
        }
        else {
            plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
            timeTables = timeTableRepository.findByPlanPlanId(planId);
            for (TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTable.getTimeTableId()));
            }
        }
        response.addPlanFrame(
                planId,
                plan.getPlanName(),
                plan.getDeparture(),
                plan.getTravel().getTravelCategory().getTravelCategoryName(),
                plan.getTravel().getTravelId(),
                plan.getTravel().getTravelName(),
                plan.getAdultCount(),
                plan.getChildCount(),
                plan.getTransportationCategory().getTransportationCategoryId());

        for (TimeTable timeTable : timeTables){
            response.addTimetable(timeTable.getTimeTableId(), timeTable.getDate(), timeTable.getTimeTableStartTime(), timeTable.getTimeTableEndTime());
        }

        for (List<TimeTablePlaceBlock> timeTablePlaceBlock : timeTablePlaceBlocks) {
            if(timeTablePlaceBlock!=null){
                for (TimeTablePlaceBlock timeTablePlaceBlock1 : timeTablePlaceBlock) {
                    response.addPlaceBlock(
                            timeTablePlaceBlock1.getBlockId(),
                            timeTablePlaceBlock1.getTimeTable().getTimeTableId(),
                            timeTablePlaceBlock1.getPlaceCategory().getPlaceCategoryId(),
                            timeTablePlaceBlock1.getPlaceName(),
                            timeTablePlaceBlock1.getPlaceTheme(),
                            timeTablePlaceBlock1.getPlaceRating(),
                            timeTablePlaceBlock1.getPlaceAddress(),
                            timeTablePlaceBlock1.getPlaceLink(),
                            timeTablePlaceBlock1.getXLocation(),
                            timeTablePlaceBlock1.getYLocation(),
                            timeTablePlaceBlock1.getBlockStartTime(),
                            timeTablePlaceBlock1.getBlockEndTime()
                    );
                }
            }
        }
        return response; // DTO 변환
    }

    @Transactional
    public EditPlanNameResponse EditPlanName(int userId, int planId, String name){
        EditPlanNameResponse response = new EditPlanNameResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        boolean exists = planRepository.existsByUser_UserIdAndPlanName(userId, name);

        if(userId != plan.getUser().getUserId()){
            response.setEdited(false);
            response.setMessage("이름 변경 권한이 없습니다.");
            return response;
        }

        if(exists && !plan.getPlanName().equals(name)) {
            response.setEdited(false);
            response.setMessage("이미 동일한 이름의 일정이 존재합니다.");
            return response;
        }

        plan.changePlanName(name);
        planRepository.save(plan);
        response.setEdited(true);
        response.setMessage("성공적으로 일정 이름을 변경하였습니다");
        return response;
    }

    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 0);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " "+ plan.getTravel().getTravelName();
        response.addPlace(googleMap.getTourPlace(travelName, preferredThemeNames));
        return response;
    }
    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 1);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " "+ plan.getTravel().getTravelName();
        response.addPlace(googleMap.getLodgingPlace(travelName, preferredThemeNames));
        return response;
    }
    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        List<PreferredTheme> preferredThemes = userRepository.findById(userId).get().getPreferredThemes();
        preferredThemes.removeIf(preferredTheme -> preferredTheme.getPreferredThemeCategory().getPreferredThemeCategoryId() != 2);

        List<String> preferredThemeNames = new ArrayList<>();
        for (PreferredTheme preferredTheme : preferredThemes) {
            preferredThemeNames.add(preferredTheme.getPreferredThemeName());
        }
        String travelName = travelCategoryName + " "+ plan.getTravel().getTravelName();
        response.addPlace(googleMap.getRestaurantPlace(travelName, preferredThemeNames));
        return response;
    }

    public PlaceResponse getSearchPlace(int userId, int planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        response.addPlace(googleMap.getSearchPlace(query));
        return response;
    }
    @Transactional
    public SavePlanResponse savePlan(int userId, int planId, String departure, int transportationCategoryId, int adultCount, int childCount, List<TimetableVO> timetables, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockLists) {
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        TransportationCategory transportationCategory = transportationCategoryRepository.findById(transportationCategoryId).get();
        plan.changeDeparture(departure);
        plan.changeTransportationCategory(transportationCategory);
        plan.updateCounts(adultCount, childCount);

        List<TimeTable> timeTables = changeTimetable(plan, timetables);
        changeTimetablePlaceBlock(plan, timetablePlaceBlockLists, timeTables);

        try {
            planRepository.save(plan);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("입력 데이터가 유효하지 않습니다.", ex);
        }
        transportationCategoryRepository.save(transportationCategory);
        SavePlanResponse response = new SavePlanResponse();
        return response;
    }

    private List<TimeTable> changeTimetable(Plan plan, List<TimetableVO> timetables) {
        if(timetables == null || timetables.isEmpty()) {
            return new ArrayList<>();
        }
        timeTableRepository.deleteByPlan(plan);
        List<TimeTable> afterTimeTables = new ArrayList<>();
        for (TimetableVO timetable : timetables) {
            afterTimeTables.add(TimeTable.builder()
                    .date(timetable.getDate())
                    .timeTableStartTime(LocalTime.of(9,0))
                    .timeTableEndTime(LocalTime.of(20,0))
                    .plan(plan)
                    .build());
            timeTableRepository.saveAll(afterTimeTables);
        }
        return afterTimeTables;
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
                        .placeTheme("")
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
    public DeletePlanResponse deletePlan(int userId, int planId) {
        DeletePlanResponse response = new DeletePlanResponse();

        if (!planRepository.existsById(planId)) {
            response.setMessage("해당 플랜이 존재하지 않습니다.");
            return response;
        }

        boolean isOwner = planRepository.existsByPlanIdAndUserUserId(planId, userId);
        if (!isOwner) {
            response.setMessage("일정을 삭제할 권한이 없습니다.");
        } else {
            planRepository.deleteById(planId);
            response.setMessage("일정을 삭제했습니다.");
        }

        return response;
    }

    public GetCompletePlanResponse getCompletePlan(int planId) {
        GetCompletePlanResponse response = new GetCompletePlanResponse();
        Plan plan = redisService.getPlan(planId);
        List<TimeTable> timeTables;
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();
        if(plan != null) {
            timeTables = redisService.getTimeTableByPlanId(planId);
            for(TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(redisService.getTimeTablePlaceBlockByTimeTableId(timeTable.getTimeTableId()));
            }
        }
        else {
            plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));
            timeTables = timeTableRepository.findByPlanPlanId(planId);
            for (TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTable.getTimeTableId()));
            }
        }
        response.addPlanFrame(
                planId,
                plan.getPlanName(),
                plan.getDeparture(),
                plan.getTravel().getTravelCategory().getTravelCategoryName(),
                plan.getTravel().getTravelId(),
                plan.getTravel().getTravelName(),
                plan.getAdultCount(),
                plan.getChildCount(),
                plan.getTransportationCategory().getTransportationCategoryId());

        for (TimeTable timeTable : timeTables){
            response.addTimetable(timeTable.getTimeTableId(), timeTable.getDate(), timeTable.getTimeTableStartTime(), timeTable.getTimeTableEndTime());
        }

        for (List<TimeTablePlaceBlock> timeTablePlaceBlock : timeTablePlaceBlocks) {
            if(timeTablePlaceBlock!=null){
                for (TimeTablePlaceBlock timeTablePlaceBlock1 : timeTablePlaceBlock) {
                    response.addPlaceBlock(
                            timeTablePlaceBlock1.getBlockId(),
                            timeTablePlaceBlock1.getTimeTable().getTimeTableId(),
                            timeTablePlaceBlock1.getPlaceCategory().getPlaceCategoryId(),
                            timeTablePlaceBlock1.getPlaceName(),
                            timeTablePlaceBlock1.getPlaceTheme(),
                            timeTablePlaceBlock1.getPlaceRating(),
                            timeTablePlaceBlock1.getPlaceAddress(),
                            timeTablePlaceBlock1.getPlaceLink(),
                            timeTablePlaceBlock1.getXLocation(),
                            timeTablePlaceBlock1.getYLocation(),
                            timeTablePlaceBlock1.getBlockStartTime(),
                            timeTablePlaceBlock1.getBlockEndTime()
                    );
                }
            }
        }
        response.setMessage("성공적으로 일정 완성본을 전송하였습니다.");
        return response;
    }

    @Transactional
    public RemoveEditorAccessByOwnerResponse removeEditorAccessByOwner(int ownerId, int planId, int targetUserId) {
        RemoveEditorAccessByOwnerResponse response = new RemoveEditorAccessByOwnerResponse();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("플랜이 존재하지 않습니다."));

        if (!plan.getUser().getUserId().equals(ownerId)) {
            throw new AccessDeniedException("플랜 소유자만 편집 권한을 삭제할 수 있습니다.");
        }

        PlanEditor planEditor = planEditorRepository.findByUser_UserIdAndPlan_PlanId(targetUserId, planId)
                .orElseThrow(() -> new AccessDeniedException("해당 편집 권한이 존재하지 않습니다."));

        planEditorRepository.delete(planEditor);

        response.setMessage("성공적으로 편집 권한을 삭제하였습니다");
        return response;
    }

    @Transactional
    public ResignEditorAccessResponse resignEditorAccess(int userId, int planId) {
        ResignEditorAccessResponse response = new ResignEditorAccessResponse();

        PlanEditor planEditor = planEditorRepository.findByUser_UserIdAndPlan_PlanId(userId, planId).orElseThrow(() -> new IllegalArgumentException("해당 편집 권한이 존재하지 않습니다."));

        planEditorRepository.delete(planEditor);

        response.setMessage("성공적으로 편집 권한을 삭제하였습니다");
        return response;
    }

    @Transactional(readOnly = true)
    public GetEditorsResponse getEditors(int userId, int planId) {
        GetEditorsResponse response = new GetEditorsResponse();

        if (!planRepository.existsByPlanIdAndUserUserId(planId, userId) &&
                !planEditorRepository.existsByUser_UserIdAndPlan_PlanId(userId, planId)) {
            throw new AccessDeniedException("해당 일정에 대한 접근 권한이 없습니다.");
        }

        List<PlanEditor> editors = planEditorRepository.findByPlan_PlanId(planId);

        for (PlanEditor editor : editors) {
            User editorDetail = editor.getUser();
            response.addSimpleEditorVO(editorDetail.getUserId(), editorDetail.getNickname());
        }

        response.setMessage("성공적으로 편집자 목록을 가져왔습니다.");
        return response;
    }

}
