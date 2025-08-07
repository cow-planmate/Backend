package com.example.planmate.controller;

import com.example.planmate.dto.*;
import com.example.planmate.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collaboration-requests")
public class CollaborationRequestController {
    private final AcceptRequestService acceptRequestService;
    private final RejectRequestService rejectRequestService;
    private final GetReceivedPendingRequestsService getReceivedPendingRequestsService;

    @PostMapping("/{collaborationRequestId}/accept")
    public ResponseEntity<AcceptRequestResponse> acceptRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        AcceptRequestResponse response = acceptRequestService.acceptRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{collaborationRequestId}/reject")
    public ResponseEntity<RejectRequestResponse> rejectRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RejectRequestResponse response = rejectRequestService.rejectRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/pending")
    public ResponseEntity<GetReceivedPendingRequestsResponse> getReceivedPendingRequests(Authentication authentication) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        GetReceivedPendingRequestsResponse response = getReceivedPendingRequestsService.getReceivedPendingRequests(userId);
        return ResponseEntity.ok(response);
    }
}
