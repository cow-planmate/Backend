package com.example.planmate.common.externalAPI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.weather.dto.SimpleWeatherInfo;
import com.example.planmate.domain.weather.dto.WeatherResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleWeather {

    @Value("${api.google.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResponse getWeatherRecommendations(String city, String startDateStr, String endDateStr) {
        try {
            // 1. Get Lat/Lng for the city
            Map<String, Double> location = getDestinationLocation(city);
            if (location == null) {
                return createFallbackResponse(startDateStr, endDateStr);
            }

            double lat = location.get("lat");
            double lng = location.get("lng");

            LocalDate start = LocalDate.parse(startDateStr);
            LocalDate end = LocalDate.parse(endDateStr);
            LocalDate today = LocalDate.now();
            LocalDate forecastLimit = today.plusDays(15);

            String url;
            // 16일 이후의 날짜가 포함되어 있으면 과거 데이터(작년) API 사용
            if (end.isAfter(forecastLimit)) {
                url = String.format(
                    "https://archive-api.open-meteo.com/v1/archive?latitude=%f&longitude=%f&daily=weathercode,temperature_2m_max,temperature_2m_min,apparent_temperature_max&timezone=auto&start_date=%s&end_date=%s",
                    lat, lng, start.minusYears(1).toString(), end.minusYears(1).toString()
                );
            } else {
                // 16일 이내면 실시간 예보 API 사용
                url = String.format(
                    "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&daily=weathercode,temperature_2m_max,temperature_2m_min,apparent_temperature_max&timezone=auto&start_date=%s&end_date=%s",
                    lat, lng, startDateStr, endDateStr
                );
            }

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Weather API Error: {} - {}", response.getStatusCode(), response.getBody());
                return createFallbackResponse(startDateStr, endDateStr);
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode daily = root.path("daily");
            JsonNode timeArr = daily.path("time");
            JsonNode weatherCodeArr = daily.path("weathercode");
            JsonNode tempMaxArr = daily.path("temperature_2m_max");
            JsonNode tempMinArr = daily.path("temperature_2m_min");
            JsonNode feelsLikeArr = daily.path("apparent_temperature_max");

            List<SimpleWeatherInfo> weatherList = new ArrayList<>();

            for (int i = 0; i < timeArr.size(); i++) {
                // 결과 데이터의 날짜를 요청한 날짜(올해)로 매핑
                LocalDate resultDate = LocalDate.parse(timeArr.get(i).asText());
                String displayDate = end.isAfter(forecastLimit) ? resultDate.plusYears(1).toString() : resultDate.toString();
                
                int code = weatherCodeArr.get(i).asInt();
                double maxTemp = tempMaxArr.get(i).asDouble();
                double minTemp = tempMinArr.get(i).asDouble();
                double feelsLike = feelsLikeArr.get(i).asDouble();

                weatherList.add(SimpleWeatherInfo.builder()
                        .date(displayDate)
                        .description(getWeatherDescription(code))
                        .temp_min(Math.round(minTemp))
                        .temp_max(Math.round(maxTemp))
                        .feels_like(Math.round(feelsLike))
                        .build());
            }

            double totalAvgTemp = weatherList.stream().mapToDouble(SimpleWeatherInfo::getFeels_like).average().orElse(15.0);
            double totalMin = weatherList.stream().mapToDouble(SimpleWeatherInfo::getTemp_min).min().orElse(15.0);
            double totalMax = weatherList.stream().mapToDouble(SimpleWeatherInfo::getTemp_max).max().orElse(15.0);

            return WeatherResponse.builder()
                    .weather(weatherList)
                    .recommendation(String.format("평균 기온 %.1f°C (최저 %.1f°C / 최고 %.1f°C)", totalAvgTemp, totalMin, totalMax))
                    .build();

        } catch (Exception e) {
            log.error("Error fetching weather: ", e);
            return createFallbackResponse(startDateStr, endDateStr);
        }
    }

    private String getWeatherDescription(int code) {
        switch (code) {
            case 0: return "맑음";
            case 1: return "대체로 맑음";
            case 2: return "구름 조금";
            case 3: return "흐림";
            case 45: case 48: return "안개";
            case 51: case 53: case 55: return "이슬비";
            case 61: case 63: case 65: return "비";
            case 71: case 73: case 75: return "눈";
            case 80: case 81: case 82: return "소나기";
            case 95: case 96: case 99: return "뇌우";
            default: return "정보 없음";
        }
    }

    private Map<String, Double> getDestinationLocation(String city) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + city + "&key=" + googleApiKey;
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                JsonNode location = results.get(0).path("geometry").path("location");
                Map<String, Double> map = new HashMap<>();
                map.put("lat", location.path("lat").asDouble());
                map.put("lng", location.path("lng").asDouble());
                return map;
            }
        } catch (Exception e) {
            log.error("Error getting location for city {}: ", city, e);
        }
        return null;
    }

    private WeatherResponse createFallbackResponse(String startDateStr, String endDateStr) {
        LocalDate start = LocalDate.parse(startDateStr);
        LocalDate end = LocalDate.parse(endDateStr);
        List<SimpleWeatherInfo> weatherList = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            weatherList.add(createFallbackDayInfo(date.toString()));
        }
        return WeatherResponse.builder()
                .weather(weatherList)
                .recommendation("날씨 정보를 가져올 수 없어 시즌 평균 기온으로 대체합니다.")
                .build();
    }

    private SimpleWeatherInfo createFallbackDayInfo(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int month = date.getMonthValue();
        double temp;
        String desc;
        if (month >= 6 && month <= 8) { temp = 28; desc = "더운 여름 날씨"; }
        else if (month >= 3 && month <= 5) { temp = 18; desc = "따뜻한 봄 날씨"; }
        else if (month >= 9 && month <= 11) { temp = 15; desc = "선선한 가을 날씨"; }
        else { temp = 5; desc = "추운 겨울 날씨"; }

        return SimpleWeatherInfo.builder()
                .date(dateStr)
                .description(desc)
                .temp_min(temp - 2)
                .temp_max(temp + 2)
                .feels_like(temp)
                .build();
    }
}
