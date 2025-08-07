package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.*;
import com.example.planmate.entity.*;
import com.example.planmate.externalAPI.GoogleMap;
import com.example.planmate.repository.*;
import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.valueObject.TimetableVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final CollaborationRequestRepository collaborationRequestRepository;
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
            for (TimeTablePlaceBlock timeTablePlaceBlock1 : timeTablePlaceBlock) {
                response.addPlaceBlock(
                        timeTablePlaceBlock1.getBlockId(),
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
        return response; // DTO 변환
    }

    public EditPlanNameResponse EditPlanName(int userId, int planId, String name){
        EditPlanNameResponse reponse = new EditPlanNameResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        plan.setPlanName(name);
        planRepository.save(plan);
        return reponse;
    }

    public PlaceResponse getTourPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getTourPlace(travelCategoryName + " " +travelName + " " + "관광지"));
        return response;
    }
    public PlaceResponse getLodgingPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getLodgingPlace(travelCategoryName + " " +travelName + " " + "숙소"));
        return response;
    }
    public PlaceResponse getRestaurantPlace(int userId, int planId) throws IOException {
        PlaceResponse response = new PlaceResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        String travelCategoryName = plan.getTravel().getTravelCategory().getTravelCategoryName();
        String travelName = plan.getTravel().getTravelName();
        response.addPlace(googleMap.getRestaurantPlace(travelCategoryName + " " +travelName + " " + "식당"));
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
        plan.setDeparture(departure);
        plan.setTransportationCategory(transportationCategory);
        plan.setAdultCount(adultCount);
        plan.setChildCount(childCount);

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
    public InviteUserToPlanResponse inviteUserToPlan(int senderId, int planId, String receiverNickname) {
        InviteUserToPlanResponse response = new InviteUserToPlanResponse();

        // 1. 사용자와 플랜 유효성 검증
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(senderId, planId);
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 유저가 존재하지 않습니다."));

        // 2. 닉네임으로 받는 유저 조회
        User receiver = userRepository.findByNickname(receiverNickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 유저가 존재하지 않습니다."));

        if (planEditorRepository.existsByUserAndPlan(receiver, plan)) {
            throw new IllegalStateException("이미 편집 권한이 있는 유저입니다.");
        }

        // 3. 이미 초대한 적이 있는지 확인 (PENDING 상태)
        Optional<CollaborationRequest> existingRequest =
                collaborationRequestRepository.findBySenderAndReceiverAndPlanAndTypeAndStatus(
                        sender, receiver, plan, CollaborationRequestType.INVITE, CollaborationRequestStatus.PENDING
                );

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("이미 초대한 유저입니다.");
        }

        // 4. CollaborationRequest 생성
        CollaborationRequest request = CollaborationRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .plan(plan)
                .type(CollaborationRequestType.INVITE)
                .status(CollaborationRequestStatus.PENDING)
                .build();

        collaborationRequestRepository.save(request);

        response.setMessage("성공적으로 초대 메세지를 보냈습니다.");

        return response;
    }
    @Transactional
    public RequestEditAccessResponse requestEditAccess(int senderId, int planId) {
        RequestEditAccessResponse response = new RequestEditAccessResponse();

        // 1. 사용자와 플랜 유효성 검증
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(senderId, planId);

        // 2. 유저 조회
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("요청한 유저가 존재하지 않습니다."));

        // 3. 플랜의 작성자(owner)가 존재하는지 확인
        User owner = plan.getUser();
        if (owner == null) {
            throw new IllegalStateException("플랜의 소유자가 존재하지 않습니다.");
        }

        // 4. 본인이 본인에게 요청하지 못하도록 막기
        if (sender.getUserId().equals(owner.getUserId())) {
            throw new IllegalArgumentException("자신에게는 권한 요청을 보낼 수 없습니다.");
        }

        if (planEditorRepository.existsByUserAndPlan(sender, plan)) {
            throw new IllegalStateException("이미 편집 권한이 있는 유저입니다.");
        }

        // 5. 이미 권한 요청 보냈는지 확인 (PENDING 상태)
        Optional<CollaborationRequest> existingRequest =
                collaborationRequestRepository.findBySenderAndReceiverAndPlanAndTypeAndStatus(
                        sender, owner, plan, CollaborationRequestType.REQUEST, CollaborationRequestStatus.PENDING
                );

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("이미 권한 요청을 보낸 상태입니다.");
        }

        // 6. CollaborationRequest 생성 및 저장
        CollaborationRequest request = CollaborationRequest.builder()
                .sender(sender)
                .receiver(owner)
                .plan(plan)
                .type(CollaborationRequestType.REQUEST)
                .status(CollaborationRequestStatus.PENDING)
                .build();

        collaborationRequestRepository.save(request);

        response.setMessage("성공적으로 권한 요청을 보냈습니다.");

        return response;
    }

    public GetCompletePlanResponse getCompletePlan(int planId) {
        GetCompletePlanResponse response = new GetCompletePlanResponse();
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));
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

        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();

        for (TimeTable timeTable : timeTables) {
            timeTablePlaceBlocks.add(timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTable.getTimeTableId()));
        }

        for (TimeTable timeTable : timeTables){
            response.addTimetable(timeTable.getTimeTableId(), timeTable.getDate(), timeTable.getTimeTableStartTime(), timeTable.getTimeTableEndTime());
        }

        for (List<TimeTablePlaceBlock> timeTablePlaceBlock : timeTablePlaceBlocks) {
            for (TimeTablePlaceBlock timeTablePlaceBlock1 : timeTablePlaceBlock) {
                response.addPlaceBlock(timeTablePlaceBlock1.getBlockId(),
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
        response.setMessage("성공적으로 일정 완성본을 전송하였습니다.");
        return response;
    }

    @Transactional
    public RemoveEditorAccessByOwnerResponse removeEditorAccessByOwner(int ownerId, int planId, int targetUserId) {
        RemoveEditorAccessByOwnerResponse response = new RemoveEditorAccessByOwnerResponse();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("플랜이 존재하지 않습니다."));

        if (!plan.getUser().getUserId().equals(ownerId)) {
            throw new SecurityException("플랜 소유자만 편집 권한을 삭제할 수 있습니다.");
        }

        PlanEditor planEditor = planEditorRepository.findByUser_UserIdAndPlan_PlanId(targetUserId, planId)
                .orElseThrow(() -> new IllegalArgumentException("해당 편집 권한이 존재하지 않습니다."));

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
            throw new IllegalArgumentException("해당 일정에 대한 접근 권한이 없습니다.");
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
