package com.example.planmate.domain.plan.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.util.Pair;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.exception.UserNotFoundException;
import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.externalAPI.GooglePlaceDetails;
import com.example.planmate.common.valueObject.LodgingPlaceVO;
import com.example.planmate.common.valueObject.RestaurantPlaceVO;
import com.example.planmate.common.valueObject.SearchPlaceVO;
import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.common.valueObject.TourPlaceVO;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.dto.DeleteMultiplePlansResponse;
import com.example.planmate.domain.plan.dto.DeletePlanResponse;
import com.example.planmate.domain.plan.dto.EditPlanNameResponse;
import com.example.planmate.domain.plan.dto.GetCompletePlanResponse;
import com.example.planmate.domain.plan.dto.GetEditorsResponse;
import com.example.planmate.domain.plan.dto.GetPlanResponse;
import com.example.planmate.domain.plan.dto.GetShareLinkResponse;
import com.example.planmate.domain.plan.dto.MakePlanResponse;
import com.example.planmate.domain.plan.dto.PlaceResponse;
import com.example.planmate.domain.plan.dto.RemoveEditorAccessByOwnerResponse;
import com.example.planmate.domain.plan.dto.ResignEditorAccessResponse;
import com.example.planmate.domain.plan.dto.SavePlanResponse;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.PlanShare;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.PlanShareRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.PreferredTheme;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.example.planmate.domain.webSocket.service.RedisService;

import lombok.RequiredArgsConstructor;

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
    private final PlanShareRepository planShareRepository;
    private final RedisService redisService;
    private final GoogleMap googleMap;
    private final GooglePlaceDetails googlePlaceDetails;
    private final PlacePhotoRepository placePhotoRepository;
    private final com.example.planmate.domain.webSocket.service.PresenceTrackingService presenceTrackingService;


    public MakePlanResponse makeService(int userId, String departure, int travelId, int transportationCategoryId, List<LocalDate> dates, int adultCount, int childCount) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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
    Plan plan = redisService.findPlanByPlanId(planId);
        List<TimeTable> timeTables;
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();
        if(plan != null) {
            timeTables = redisService.findTimeTablesByPlanId(planId);
            for(TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(redisService.findTimeTablePlaceBlocksByTimeTableId(timeTable.getTimeTableId()));
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
                            timeTablePlaceBlock1.getPlacePhoto().getPlaceId(),
                            timeTablePlaceBlock1.getXLocation(),
                            timeTablePlaceBlock1.getYLocation(),
                            timeTablePlaceBlock1.getBlockStartTime(),
                            timeTablePlaceBlock1.getBlockEndTime()
                    );
                }
            }
        }
    response.setUserDayIndexes(presenceTrackingService.getPlanTracker(planId));
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
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " "+ travelName, preferredThemeNames);
        List<TourPlaceVO> tourPlaceVOs = (List<TourPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(tourPlaceVOs);
        response.addNextPageToken(pair.getSecond());
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
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " "+ travelName, preferredThemeNames);
        List<LodgingPlaceVO> lodgingPlaceVOs = (List<LodgingPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(lodgingPlaceVOs);
        response.addNextPageToken(pair.getSecond());
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
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap.getRestaurantPlace(travelCategoryName + " "+ travelName, preferredThemeNames);
        List<RestaurantPlaceVO> restaurantPlaceVOs = (List<RestaurantPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(restaurantPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getSearchPlace(int userId, int planId, String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        List<SearchPlaceVO> searchPlaceVOs = (List<SearchPlaceVO>) googlePlaceDetails.searchGooglePlaceDetailsAsyncBlocking(pair.getFirst());
        response.addPlace(searchPlaceVOs);
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getTourPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<TourPlaceVO>, List<String>> pair = googleMap.getTourPlace(travelCategoryName + " "+ travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }
    public PlaceResponse getLodgingPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<LodgingPlaceVO>, List<String>> pair = googleMap.getLodgingPlace(travelCategoryName + " "+ travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }
    public PlaceResponse getRestaurantPlace(String travelCategoryName, String travelName) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<RestaurantPlaceVO>, List<String>> pair = googleMap.getRestaurantPlace(travelCategoryName + " "+ travelName, new ArrayList<>());
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getSearchPlace(String query) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getSearchPlace(query);
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }

    public PlaceResponse getNextPlace(List<String> nextPageToken) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Pair<List<SearchPlaceVO>, List<String>> pair = googleMap.getNextPagePlace(nextPageToken);
        response.addPlace(pair.getFirst());
        response.addNextPageToken(pair.getSecond());
        return response;
    }


    @Transactional
    public SavePlanResponse savePlan(int userId, String departure, int travelId, int transportationCategoryId, int adultCount, int childCount, List<TimetableVO> timetableVOs, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockVOLists) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

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

        List<TimeTable> timeTables = saveTimetable(plan, timetableVOs);
        saveTimetablePlaceBlock(timeTables, timetablePlaceBlockVOLists);
        SavePlanResponse savePlanResponse = new SavePlanResponse(savedPlan.getPlanId());
        return savePlanResponse;
    }

    private List<TimeTable> saveTimetable(Plan plan, List<TimetableVO> timetableVOs) {
        if(timetableVOs == null || timetableVOs.isEmpty()) {
            return new ArrayList<>();
        }
        List<TimeTable> timeTables = new ArrayList<>();
        for (TimetableVO timetable : timetableVOs) {
            timeTables.add(TimeTable.builder()
                    .date(timetable.getDate())
                    .timeTableStartTime(timetable.getStartTime())
                    .timeTableEndTime(timetable.getEndTime())
                    .plan(plan)
                    .build());
            timeTableRepository.saveAll(timeTables);
        }
        return timeTables;
    }
    private void saveTimetablePlaceBlock(List<TimeTable> timeTableVOs, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockVOLists) {
        if(timetablePlaceBlockVOLists == null || timetablePlaceBlockVOLists.isEmpty()) {
            return;
        }
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = new ArrayList<>();
        for(int i = 0; i < timetablePlaceBlockVOLists.size(); i++){
            if(timeTableVOs.isEmpty()){
                break;
            }
            TimeTable timetable = timeTableVOs.get(i);
            for(int j = 0; j < timetablePlaceBlockVOLists.get(i).size(); j++){
                TimetablePlaceBlockVO timeTablePlaceBlockVO = timetablePlaceBlockVOLists.get(i).get(j);
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
            .placePhoto(placePhotoRepository.getReferenceById(timeTablePlaceBlockVO.getPlaceId()))
                        .placeCategory(placeCategory)
                        .build());
            }
        }
        timeTablePlaceBlockRepository.saveAll(timeTablePlaceBlocks);
    }
    @Transactional
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
    @Transactional
    public DeleteMultiplePlansResponse deleteMultiplePlans(int userId, List<Integer> planIds) {
        DeleteMultiplePlansResponse response = new DeleteMultiplePlansResponse();

        if (planIds == null || planIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 일정 ID가 비어 있습니다.");
        }

        List<Plan> ownedPlans = planRepository.findAllByPlanIdInAndUserUserId(planIds, userId);

        if (ownedPlans == null || ownedPlans.isEmpty()) {
            throw new AccessDeniedException("삭제할 권한이 있는 일정이 없습니다.");
        }

        planRepository.deleteAllInBatch(ownedPlans);

        response.setMessage("일정을 삭제했습니다.");
        return response;
    }

    public GetCompletePlanResponse getCompletePlan(int planId) {
        GetCompletePlanResponse response = new GetCompletePlanResponse();

    Plan plan = redisService.findPlanByPlanId(planId);
        List<TimeTable> timeTables;
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();
        if(plan != null) {
            timeTables = redisService.findTimeTablesByPlanId(planId);
            for(TimeTable timeTable : timeTables) {
                timeTablePlaceBlocks.add(redisService.findTimeTablePlaceBlocksByTimeTableId(timeTable.getTimeTableId()));
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
                            timeTablePlaceBlock1.getPlacePhoto().getPlaceId(),
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

    @Transactional
    public GetShareLinkResponse getShareLink(int userId, int planId) {
        GetShareLinkResponse response = new GetShareLinkResponse();

        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);

        Optional<PlanShare> existingShare = planShareRepository.findById(planId);
        if (existingShare.isPresent()) {
            response.setSharedPlanUrl(buildShareUrl(planId, existingShare.get().getShareToken()));
            response.setMessage("성공적으로 링크를 받아왔습니다");
            return response;
        }

        String token = UUID.randomUUID().toString();

        PlanShare planShare = PlanShare.builder()
                .plan(plan)
                .shareToken(token)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        planShareRepository.save(planShare);

        response.setSharedPlanUrl(buildShareUrl(planShare.getPlanId(), planShare.getShareToken()));
        response.setMessage("성공적으로 링크를 받아왔습니다");
        return response;
    }

    private String buildShareUrl(int planId, String token) {
        return "https://www.planmate.site/complete?id=" + planId + "&token=" + token;
    }

    @Transactional
    public void validateShareToken(int planId, String shareToken) {
        if (shareToken == null || shareToken.isBlank()) {
            throw new IllegalArgumentException("유효한 공유 토큰이 필요합니다.");
        }

        PlanShare planShare = planShareRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("공유 링크가 존재하지 않습니다."));

        if (!planShare.getIsActive() || !planShare.getShareToken().equals(shareToken)) {
            throw new IllegalArgumentException("공유 링크가 유효하지 않거나 일정과 일치하지 않습니다.");
        }
    }

}
