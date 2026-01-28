package com.example.planmate.domain.collaborationRequest.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.collaborationRequest.dto.AcceptRequestResponse;
import com.example.planmate.domain.collaborationRequest.dto.GetReceivedPendingRequestsResponse;
import com.example.planmate.domain.collaborationRequest.dto.RejectRequestResponse;
import com.example.planmate.domain.collaborationRequest.service.CollaborationRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Collaboration", description = "협업 요청(초대/수락/거절) 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collaboration-requests")
public class CollaborationRequestController {
    private final CollaborationRequestService collaborationRequestService;

    @Operation(summary = "협업 요청 수락", description = "초대받은 협업 요청이나 편집 권한 요청을 수락합니다.")
    @PostMapping("/{collaborationRequestId}/accept")
    public ResponseEntity<AcceptRequestResponse> acceptRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        AcceptRequestResponse response = collaborationRequestService.acceptRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "협업 요청 거절", description = "초대받은 협업 요청이나 편집 권한 요청을 거절합니다.")
    @PostMapping("/{collaborationRequestId}/reject")
    public ResponseEntity<RejectRequestResponse> rejectRequest(Authentication authentication, @PathVariable("collaborationRequestId") int collaborationRequestId) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        RejectRequestResponse response = collaborationRequestService.rejectRequest(userId, collaborationRequestId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대기 중인 요청 조회", description = "사용자가 받은 아직 처리되지 않은 협업 요청 목록을 조회합니다.")
    @GetMapping("/pending")
    public ResponseEntity<GetReceivedPendingRequestsResponse> getReceivedPendingRequests(Authentication authentication) throws IOException {
        int userId = Integer.parseInt(authentication.getName());
        GetReceivedPendingRequestsResponse response = collaborationRequestService.getReceivedPendingRequests(userId);
        return ResponseEntity.ok(response);
    }
}
