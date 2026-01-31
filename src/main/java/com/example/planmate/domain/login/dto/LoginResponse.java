package com.example.planmate.domain.login.dto;

import com.example.planmate.common.dto.CommonResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "로그인 응답 데이터")
public class LoginResponse extends CommonResponse {
    @Schema(description = "로그인 성공 여부", example = "true")
    private boolean loginSuccess;

    @Schema(description = "사용자 고유 식별자", example = "1")
    private int userId;

    @Schema(description = "사용자 닉네임", example = "플랜메이트")
    private String nickname;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1...")
    private String refreshToken;
}
