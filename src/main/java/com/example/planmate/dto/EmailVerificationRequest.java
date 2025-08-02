package com.example.planmate.dto;

import com.example.planmate.gita.EmailVerificationPurpose;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailVerificationRequest {
    private String email;
    private EmailVerificationPurpose purpose;
    private int verificationCode;
}
