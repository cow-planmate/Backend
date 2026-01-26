package com.example.planmate.domain.login.dto;

import com.example.planmate.common.dto.IRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 요청 데이터")
public class LoginRequest implements IRequest {
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "사용자 비밀번호", example = "password123")
    private String password;
}
