package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "이메일 인증 코드 검증 요청 데이터")
public class EmailVerificationRequest {
    @Schema(description = "인증할 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "인증 목적")
    private EmailVerificationPurpose purpose;

    @Schema(description = "인증 코드", example = "123456")
    private int verificationCode;
}
