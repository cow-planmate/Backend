package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationResponse extends CommonResponse {
    private boolean emailVerified;
    private String token;
}
