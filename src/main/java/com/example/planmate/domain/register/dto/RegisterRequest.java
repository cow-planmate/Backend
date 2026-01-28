package com.example.planmate.domain.register.dto;

import com.example.planmate.common.dto.IRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원가입 요청 데이터")
public class RegisterRequest implements IRequest {
    @Schema(description = "사용자 닉네임", example = "플랜메이트")
    private String nickname;

    @Schema(description = "사용자 비밀번호", example = "password123")
    private String password;

    @Schema(description = "성별 (0: 남성, 1: 여성)", example = "0")
    private int gender;

    @Schema(description = "나이대 (예: 20)", example = "20")
    private int age;
}
