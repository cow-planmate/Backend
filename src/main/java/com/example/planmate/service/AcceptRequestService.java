package com.example.planmate.service;

import com.example.planmate.auth.CollaborationRequestValidator;
import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.AcceptRequestResponse;
import com.example.planmate.dto.InviteUserToPlanResponse;
import com.example.planmate.entity.*;
import com.example.planmate.repository.CollaborationRequestRepository;
import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AcceptRequestService {
    private final CollaborationRequestValidator collaborationRequestValidator;
    private final PlanEditorRepository planEditorRepository;

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
}
