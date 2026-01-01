package com.example.planmate.domain.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleWeatherInfo {
    private String date;
    private String description;
    private double temp_min;
    private double temp_max;
    private double feels_like;
}
