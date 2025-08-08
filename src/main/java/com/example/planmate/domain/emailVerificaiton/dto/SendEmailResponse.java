package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailResponse extends CommonResponse {
    boolean isVerificationSent;
}
