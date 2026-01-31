package com.example.planmate.domain.weather.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "날씨 정보 조회 응답 데이터")
public class WeatherResponse {
    @Schema(description = "일별 날씨 정보 목록")
    private List<SimpleWeatherInfo> weather;

    @Schema(description = "날씨에 따른 추천 메시지", example = "맑은 날씨니 가벼운 옷차림을 추천합니다.")
    private String recommendation;
}
