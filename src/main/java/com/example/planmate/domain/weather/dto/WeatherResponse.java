package com.example.planmate.domain.weather.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponse {
    private List<SimpleWeatherInfo> weather;
    private String recommendation;
}
