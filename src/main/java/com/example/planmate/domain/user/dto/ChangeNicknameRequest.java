package com.example.planmate.domain.user.dto;

import com.example.planmate.common.dto.IRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "닉네임 변경 요청 데이터")
public class ChangeNicknameRequest implements IRequest {
    @Schema(description = "새로운 닉네임", example = "새닉네임")
    private String nickname;
}
