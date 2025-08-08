package com.example.planmate.auth;

import com.example.planmate.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.collaborationRequest.entity.CollaborationRequestStatus;
import com.example.planmate.collaborationRequest.repository.CollaborationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CollaborationRequestValidator {
    private final CollaborationRequestRepository collaborationRequestRepository;

    public CollaborationRequest validateReceiverAndPending(int receiverId, int collaborationRequestId) {
        CollaborationRequest request = collaborationRequestRepository.findById(collaborationRequestId)
                .orElseThrow(() -> new IllegalArgumentException("해당 요청이 존재하지 않습니다."));

        if (!request.getReceiver().getUserId().equals(receiverId)) {
            throw new IllegalArgumentException("해당 요청을 처리할 권한이 없습니다.");
        }

        if (!request.getStatus().equals(CollaborationRequestStatus.PENDING)) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        return request;
    }
}
