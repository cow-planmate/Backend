package com.example.planmate.controller;

import com.example.planmate.dto.AcceptRequestResponse;
import com.example.planmate.dto.GetReceivedPendingRequestsResponse;
import com.example.planmate.dto.RejectRequestResponse;
import com.example.planmate.service.CollaborationRequestService;
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
