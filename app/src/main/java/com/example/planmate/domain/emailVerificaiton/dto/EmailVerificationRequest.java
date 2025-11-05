package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailVerificationRequest {
    private String email;
    private EmailVerificationPurpose purpose;
    private int verificationCode;
}
