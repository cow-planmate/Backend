package com.example.planmate.service;

import com.example.planmate.auth.CollaborationRequestValidator;
import com.example.planmate.dto.AcceptRequestResponse;
import com.example.planmate.dto.GetReceivedPendingRequestsResponse;
import com.example.planmate.dto.RejectRequestResponse;
import com.example.planmate.entity.CollaborationRequest;
import com.example.planmate.entity.CollaborationRequestStatus;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.entity.User;
import com.example.planmate.repository.CollaborationRequestRepository;
import com.example.planmate.repository.PlanEditorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollaborationRequestService {
    private final CollaborationRequestValidator collaborationRequestValidator;
    private final PlanEditorRepository planEditorRepository;
    private final CollaborationRequestRepository collaborationRequestRepository;
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
