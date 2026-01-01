package com.example.planmate.domain.weather.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherRequest {
    private String city;
    private String start_date;
    private String end_date;
}
