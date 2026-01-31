package com.example.planmate.domain.feedback.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.feedback.dto.BetaFeedbackRequest;
import com.example.planmate.domain.feedback.dto.BetaFeedbackResponse;
import com.example.planmate.domain.feedback.service.BetaFeedbackService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Feedback", description = "베타 테스트 피드백 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/beta/feedback")
public class BetaFeedbackController {

    private final BetaFeedbackService betaFeedbackService;

    @Operation(summary = "베타 피드백 제출", description = "베타 테스터의 의견이나 버그 제보를 수집합니다.")
    @PostMapping("")
    public ResponseEntity<BetaFeedbackResponse> create(
            @RequestBody BetaFeedbackRequest request
    ) {
        BetaFeedbackResponse response = betaFeedbackService.create(request);
        return ResponseEntity.ok(response);
    }
}
