package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailVerificationRequest {
    private String email;
    private int verificationCode;
}
