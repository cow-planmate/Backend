package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationResponse extends CommonResponse{
    private boolean emailVerified;
}
