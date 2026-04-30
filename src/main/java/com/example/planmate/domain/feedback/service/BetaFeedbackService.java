package com.example.planmate.domain.feedback.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.feedback.dto.AdminFeedbackListResponse;
import com.example.planmate.domain.feedback.dto.BetaFeedbackRequest;
import com.example.planmate.domain.feedback.dto.BetaFeedbackResponse;
import com.example.planmate.domain.feedback.entity.BetaFeedback;
import com.example.planmate.domain.feedback.repository.BetaFeedbackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BetaFeedbackService {

    private final BetaFeedbackRepository repository;

    public BetaFeedbackResponse create(BetaFeedbackRequest request) {
        BetaFeedbackResponse response = new BetaFeedbackResponse();

        if (request.getContent() == null || request.getContent().isBlank()) {
            response.setMessage("피드백 내용이 비어있습니다");
            return response;
        }

        BetaFeedback feedback = new BetaFeedback(request.getContent());
        repository.save(feedback);

        response.setMessage("피드백이 정상적으로 전송되었습니다");
        return response;
    }

    public AdminFeedbackListResponse getAllFeedbacks() {
        List<BetaFeedback> feedbacks = repository.findAll();
        AdminFeedbackListResponse response = new AdminFeedbackListResponse();
        response.setMessage("피드백 목록 조회 성공");
        response.setFeedbacks(feedbacks.stream()
                .map(f -> new AdminFeedbackListResponse.FeedbackItem(
                        f.getFeedbackId(), f.getContent(), f.getCreatedAt()))
                .collect(Collectors.toList()));
        return response;
    }
}

