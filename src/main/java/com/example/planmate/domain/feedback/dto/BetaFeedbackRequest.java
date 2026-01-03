package com.example.planmate.domain.feedback.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BetaFeedbackRequest implements IRequest {
    private String content;
}
