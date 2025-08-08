package com.example.planmate.collaborationRequest.dto;

import com.example.planmate.dto.CommonResponse;
import com.example.planmate.valueObject.PendingRequestVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetReceivedPendingRequestsResponse extends CommonResponse {
    private List<PendingRequestVO> pendingRequests;

    public GetReceivedPendingRequestsResponse() {
        this.pendingRequests = new ArrayList<>();
    }

    public void addPendingRequest(
            int requestId,
            int senderId,
            String senderNickname,
            int planId,
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
