package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailRequest {
    private String email;
    private EmailVerificationPurpose purpose;
}
