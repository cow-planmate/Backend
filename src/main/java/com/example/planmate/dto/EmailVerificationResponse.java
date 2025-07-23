package com.example.planmate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailVerificationResponse extends CommonResponse{
    private boolean emailVerified;
}
