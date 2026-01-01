package com.example.planmate.domain.weather.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.common.externalAPI.GoogleWeather;
import com.example.planmate.domain.weather.dto.WeatherRequest;
import com.example.planmate.domain.weather.dto.WeatherResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final GoogleWeather googleWeather;

    @PostMapping("/recommendations")
    public WeatherResponse getWeatherRecommendations(@RequestBody WeatherRequest request) {
        return googleWeather.getWeatherRecommendations(
            request.getCity(), 
            request.getStart_date(), 
            request.getEnd_date()
        );
    }
}
