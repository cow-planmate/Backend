package com.example.planmate.domain.feedback.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminFeedbackListResponse extends CommonResponse {

    private List<FeedbackItem> feedbacks;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class FeedbackItem {
        private Long feedbackId;
        private String content;
        private LocalDateTime createdAt;
    }
}
