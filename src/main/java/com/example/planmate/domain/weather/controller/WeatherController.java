package com.example.planmate.domain.weather.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.common.externalAPI.GoogleWeather;
import com.example.planmate.domain.weather.dto.WeatherRequest;
import com.example.planmate.domain.weather.dto.WeatherResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Weather", description = "날씨 정보 관련 API")
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final GoogleWeather googleWeather;

    @Operation(summary = "날씨 기반 추천 조회", description = "특정 도시와 날짜 범위의 날씨 정보를 가져와 여행 관련 추천을 제공합니다.")
    @PostMapping("/recommendations")
    public WeatherResponse getWeatherRecommendations(@RequestBody WeatherRequest request) {
        return googleWeather.getWeatherRecommendations(
            request.getCity(), 
            request.getStart_date(), 
            request.getEnd_date()
        );
    }
}
