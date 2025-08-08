package com.example.planmate.collaborationRequest.service;

import com.example.planmate.auth.CollaborationRequestValidator;
import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.collaborationRequest.dto.AcceptRequestResponse;
import com.example.planmate.collaborationRequest.dto.GetReceivedPendingRequestsResponse;
import com.example.planmate.collaborationRequest.dto.RejectRequestResponse;
import com.example.planmate.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.collaborationRequest.entity.CollaborationRequestStatus;
import com.example.planmate.collaborationRequest.entity.CollaborationRequestType;
import com.example.planmate.collaborationRequest.repository.CollaborationRequestRepository;
import com.example.planmate.dto.*;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.entity.User;
import com.example.planmate.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CollaborationRequestService {
    private final PlanRepository planRepository;
    private final PlanAccessValidator planAccessValidator;
    private final UserRepository userRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
    private final PlanEditorRepository planEditorRepository;
    private final CollaborationRequestValidator collaborationRequestValidator;

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

    @Transactional
    public AcceptRequestResponse acceptRequest(int receiverId, int collaborationRequestId) {
        AcceptRequestResponse response = new AcceptRequestResponse();

        CollaborationRequest request = collaborationRequestValidator.validateReceiverAndPending(receiverId, collaborationRequestId);

        request.changeStatus(CollaborationRequestStatus.getAcceptedStatus(request.getType()));

        User targetUser = request.getTargetUserForAcceptance();

        boolean alreadyEditor = planEditorRepository.existsByUserAndPlan(targetUser, request.getPlan());
        if (!alreadyEditor) {
            PlanEditor planEditor = PlanEditor.builder()
                    .user(targetUser)
                    .plan(request.getPlan())
                    .build();
            planEditorRepository.save(planEditor);
        }

        response.setMessage("성공적으로 메세지를 수락했습니다.");

        return response;
    }

    @Transactional
    public RejectRequestResponse rejectRequest(int receiverId, int collaborationRequestId) {
        RejectRequestResponse response = new RejectRequestResponse();

        CollaborationRequest request = collaborationRequestValidator.validateReceiverAndPending(receiverId, collaborationRequestId);

        request.changeStatus(CollaborationRequestStatus.getRejectedStatus(request.getType()));

        response.setMessage("성공적으로 메세지를 거절했습니다.");

        return response;
    }

    @Transactional(readOnly = true)
    public GetReceivedPendingRequestsResponse getReceivedPendingRequests(int receiverId) {
        GetReceivedPendingRequestsResponse response = new GetReceivedPendingRequestsResponse();

        List<CollaborationRequest> pendingRequests = collaborationRequestRepository
                .findByReceiver_UserIdAndStatus(receiverId, CollaborationRequestStatus.PENDING);

        for (CollaborationRequest request : pendingRequests) {
            response.addPendingRequest(
                    request.getId(),
                    request.getSender().getUserId(),
                    request.getSender().getNickname(),
                    request.getPlan().getPlanId(),
                    request.getPlan().getPlanName(),
                    request.getType().name()
            );
        }

        response.setMessage("성공적으로 사용자의 받은 메세지를 가져왔습니다.");

        return response;
    }
}
