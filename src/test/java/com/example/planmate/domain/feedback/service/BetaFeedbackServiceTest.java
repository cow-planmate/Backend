package com.example.planmate.domain.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.domain.feedback.dto.BetaFeedbackRequest;
import com.example.planmate.domain.feedback.dto.BetaFeedbackResponse;
import com.example.planmate.domain.feedback.entity.BetaFeedback;
import com.example.planmate.domain.feedback.repository.BetaFeedbackRepository;

@ExtendWith(MockitoExtension.class)
class BetaFeedbackServiceTest {

    @Mock
    private BetaFeedbackRepository repository;

    @InjectMocks
    private BetaFeedbackService betaFeedbackService;

    @Test
    @DisplayName("피드백이 정상적으로 생성된다.")
    void create_success() {
        // given
        BetaFeedbackRequest request = new BetaFeedbackRequest();
        request.setContent("테스트 피드백입니다.");

        // when
        BetaFeedbackResponse response = betaFeedbackService.create(request);

        // then
        assertEquals("피드백이 정상적으로 전송되었습니다", response.getMessage());
        verify(repository).save(any(BetaFeedback.class));
    }

    @Test
    @DisplayName("피드백 내용이 비어있으면 생성되지 않는다.")
    void create_fail_when_empty() {
        // given
        BetaFeedbackRequest request = new BetaFeedbackRequest();
        request.setContent("");

        // when
        BetaFeedbackResponse response = betaFeedbackService.create(request);

        // then
        assertEquals("피드백 내용이 비어있습니다", response.getMessage());
    }
}
