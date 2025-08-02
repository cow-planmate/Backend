package com.example.planmate.dto;

import com.example.planmate.gita.EmailVerificationPurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailRequest {
    private String email;
    private EmailVerificationPurpose purpose;
}
