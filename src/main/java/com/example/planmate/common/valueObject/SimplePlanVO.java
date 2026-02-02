package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "간략한 플랜 정보")
public class SimplePlanVO {
    @Schema(description = "플랜 ID", example = "1")
    private int planId;

    @Schema(description = "플랜 이름", example = "제주도 여행")
    private String planName;

    @Schema(description = "시작 날짜", example = "2024-03-27")
    private String startDate;

    @Schema(description = "종료 날짜", example = "2024-03-29")
    private String endDate;
}
