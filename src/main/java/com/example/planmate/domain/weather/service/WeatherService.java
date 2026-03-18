package com.example.planmate.domain.weather.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.externalAPI.GoogleWeather;
import com.example.planmate.domain.travel.service.TravelService;
import com.example.planmate.domain.weather.dto.WeatherResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final TravelService travelService;
    private final GoogleWeather googleWeather;

    @Transactional
    public WeatherResponse getWeather(String city, String startDateStr, String endDateStr) {

        Map<String, Double> location = travelService.getOrInitializeLocation(city);

        if (location == null) {
            return googleWeather.createFallbackResponse(
                    startDateStr,
                    endDateStr);
        }

        double lat = location.get("lat");
        double lng = location.get("lng");

        return googleWeather.getWeatherRecommendations(
                lat,
                lng,
                startDateStr,
                endDateStr);
    }
}