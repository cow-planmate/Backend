package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "간략한 플랜 정보")
public class SimplePlanVO {
    @Schema(description = "플랜 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String planId;

    @Schema(description = "플랜 이름", example = "제주도 여행")
    private String planName;
}
