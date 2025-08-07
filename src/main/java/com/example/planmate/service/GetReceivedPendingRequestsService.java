package com.example.planmate.service;

import com.example.planmate.auth.CollaborationRequestValidator;
import com.example.planmate.dto.GetReceivedPendingRequestsResponse;
import com.example.planmate.dto.RejectRequestResponse;
import com.example.planmate.entity.CollaborationRequest;
import com.example.planmate.entity.CollaborationRequestStatus;
import com.example.planmate.repository.CollaborationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetReceivedPendingRequestsService {
    private final CollaborationRequestRepository collaborationRequestRepository;

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
