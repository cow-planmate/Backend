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
@Schema(description = "일별 간략 날씨 정보")
public class SimpleWeatherInfo {
    @Schema(description = "날짜", example = "2023-10-01")
    private String date;

    @Schema(description = "날씨 설명", example = "맑음")
    private String description;

    @Schema(description = "최저 기온", example = "15.5")
    private double temp_min;

    @Schema(description = "최고 기온", example = "25.0")
    private double temp_max;

    @Schema(description = "체감 온도", example = "24.0")
    private double feels_like;
}
