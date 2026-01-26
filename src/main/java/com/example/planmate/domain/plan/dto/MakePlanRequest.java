package com.example.planmate.domain.plan.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "플랜 초기 생성 요청 데이터")
public class MakePlanRequest {
    @Schema(description = "출발지 주소/이름", example = "서울역")
    private String departure;

    @Schema(description = "여행지(도시) ID", example = "1")
    private int travelId;

    @JsonProperty("transportation")
    @Schema(description = "이동 수단 카테고리 ID (0: 대중교통, 1: 자동차)", example = "0")
    private int transportationCategoryId;

    @Schema(description = "일치하는 날짜 목록", example = "[\"2026-01-26\", \"2026-01-27\"]")
    private List<LocalDate> dates;

    @Schema(description = "성인 인원 수", example = "2")
    private int adultCount;

    @Schema(description = "어린이 인원 수", example = "0")
    private int childCount;
}
