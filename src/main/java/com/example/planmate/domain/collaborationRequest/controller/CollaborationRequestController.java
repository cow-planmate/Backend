package com.example.planmate.domain.collaborationRequest.controller;

import com.example.planmate.domain.collaborationRequest.dto.*;
import com.example.planmate.domain.collaborationRequest.service.CollaborationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collaboration-requests")
public class CollaborationRequestController {
    private final CollaborationRequestService collaborationRequestService;

    @PostMapping("/{planId}/invite")
    public ResponseEntity<InviteUserToPlanResponse> inviteUserToPlan(Authentication authentication, @PathVariable("planId") int planId, @RequestBody InviteUserToPlanRequest request) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        InviteUserToPlanResponse response = collaborationRequestService.inviteUserToPlan(userId, planId, request.getReceiverNickname());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{planId}/request-access")
    public ResponseEntity<RequestEditAccessResponse> requestEditAccess(Authentication authentication, @PathVariable("planId") int planId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RequestEditAccessResponse response = collaborationRequestService.requestEditAccess(userId, planId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{collaborationRequestId}/accept")
    public ResponseEntity<AcceptRequestResponse> acceptRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        AcceptRequestResponse response = collaborationRequestService.acceptRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{collaborationRequestId}/reject")
    public ResponseEntity<RejectRequestResponse> rejectRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RejectRequestResponse response = collaborationRequestService.rejectRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/pending")
    public ResponseEntity<GetReceivedPendingRequestsResponse> getReceivedPendingRequests(Authentication authentication) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        GetReceivedPendingRequestsResponse response = collaborationRequestService.getReceivedPendingRequests(userId);
        return ResponseEntity.ok(response);
    }
}
