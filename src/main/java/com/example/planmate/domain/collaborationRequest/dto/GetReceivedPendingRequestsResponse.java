package com.example.planmate.domain.collaborationRequest.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PendingRequestVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetReceivedPendingRequestsResponse extends CommonResponse {
    private List<PendingRequestVO> pendingRequests;

    public GetReceivedPendingRequestsResponse() {
        this.pendingRequests = new ArrayList<>();
    }

    public void addPendingRequest(
            int requestId,
            String senderId,
            String senderNickname,
            String planId,
            String planName,
            String type
    ) {
        pendingRequests.add(new PendingRequestVO(
                requestId,
                senderId,
                senderNickname,
                planId,
                planName,
                type
        ));
    }
}
