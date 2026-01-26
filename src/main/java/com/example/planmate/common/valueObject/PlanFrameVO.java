package com.example.planmate.common.valueObject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플랜 기본 정보 (프레임)")
public class PlanFrameVO {
    @Schema(description = "플랜 ID", example = "1")
    private int planId;

    @Schema(description = "플랜 이름", example = "나의 서울 여행")
    private String planName;

    @Schema(description = "출발지", example = "서울역")
    private String departure;

    @Schema(description = "여행 테마 이름", example = "힐링")
    private String travelCategoryName;

    @Schema(description = "여행지 ID", example = "1")
    private int travelId;

    @Schema(description = "여행지 이름", example = "서울특별시")
    private String travelName;

    @Schema(description = "성인 인원수", example = "2")
    private int adultCount;

    @Schema(description = "아동 인원수", example = "0")
    private int childCount;

    @Schema(description = "이동수단 카테고리 ID (1: 자차, 2: 대중교통)", example = "1")
    private int transportationCategoryId;
}
