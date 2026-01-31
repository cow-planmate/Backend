package com.example.planmate.domain.emailVerificaiton.dto;

import com.example.planmate.domain.emailVerificaiton.enums.EmailVerificationPurpose;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "인증 이메일 전송 요청 데이터")
public class SendEmailRequest {
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "전송 목적")
    private EmailVerificationPurpose purpose;
}
