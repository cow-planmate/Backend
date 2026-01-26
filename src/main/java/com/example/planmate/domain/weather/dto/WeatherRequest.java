package com.example.planmate.domain.weather.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "날씨 정보 조회 요청 데이터")
public class WeatherRequest {
    @Schema(description = "도시 이름", example = "Seoul")
    private String city;

    @Schema(description = "시작 날짜", example = "2023-10-01")
    private String start_date;

    @Schema(description = "종료 날짜", example = "2023-10-03")
    private String end_date;
}
