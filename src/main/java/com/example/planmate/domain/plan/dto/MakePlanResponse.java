package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "플랜 초기 생성 응답 데이터")
public class MakePlanResponse extends CommonResponse {
    @Schema(description = "생성된 플랜 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String planId;
}
