package com.example.planmate.domain.feedback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.feedback.dto.AdminFeedbackListResponse;
import com.example.planmate.domain.feedback.service.BetaFeedbackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Admin", description = "관리자 전용 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/feedbacks")
public class AdminFeedbackController {

    private final BetaFeedbackService betaFeedbackService;

    @Operation(summary = "피드백 전체 조회", description = "관리자가 모든 베타 피드백을 조회합니다.")
    @GetMapping("")
    public ResponseEntity<AdminFeedbackListResponse> getAllFeedbacks() {
        AdminFeedbackListResponse response = betaFeedbackService.getAllFeedbacks();
        return ResponseEntity.ok(response);
    }
}
