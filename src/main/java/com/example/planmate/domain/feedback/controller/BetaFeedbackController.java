package com.example.planmate.domain.feedback.controller;

import com.example.planmate.domain.feedback.dto.BetaFeedbackRequest;
import com.example.planmate.domain.feedback.dto.BetaFeedbackResponse;
import com.example.planmate.domain.feedback.service.BetaFeedbackService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/beta/feedback")
public class BetaFeedbackController {

    private final BetaFeedbackService betaFeedbackService;

    @PostMapping("")
    public ResponseEntity<BetaFeedbackResponse> create(
            @RequestBody BetaFeedbackRequest request
    ) {
        BetaFeedbackResponse response = betaFeedbackService.create(request);
        return ResponseEntity.ok(response);
    }
}
