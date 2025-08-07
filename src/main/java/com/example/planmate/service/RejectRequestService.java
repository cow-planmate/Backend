package com.example.planmate.service;

import com.example.planmate.auth.CollaborationRequestValidator;
import com.example.planmate.dto.AcceptRequestResponse;
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

@Service
@RequiredArgsConstructor
public class RejectRequestService {
    private final CollaborationRequestValidator collaborationRequestValidator;

    @Transactional
    public RejectRequestResponse rejectRequest(int receiverId, int collaborationRequestId) {
        RejectRequestResponse response = new RejectRequestResponse();

        CollaborationRequest request = collaborationRequestValidator.validateReceiverAndPending(receiverId, collaborationRequestId);

        request.changeStatus(CollaborationRequestStatus.getRejectedStatus(request.getType()));

        response.setMessage("성공적으로 메세지를 거절했습니다.");

        return response;
    }
}
