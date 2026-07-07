package com.example.planmate.common.externalAPI;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.planmate.domain.route.dto.RoutePointDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * OSRM(Open Source Routing Machine)으로 도로를 따라가는 경로를 조회한다.
 * 기본값은 공개 데모 서버이며, api.osrm.base-url 설정으로 자체 서버로 교체할 수 있다.
 */
@Component
@Slf4j
public class OsrmRouting {

    @Value("${api.osrm.base-url:https://router.project-osrm.org}")
    private String osrmBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 방문 순서대로 나열된 좌표들을 하나의 도로 경로로 연결한다.
     * 실패하면 null을 반환한다(호출 측에서 직선으로 대체).
     */
    public RoutingResult getRoute(List<RoutePointDto> waypoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return null;
        }

        // OSRM 좌표 형식: lng,lat;lng,lat;...
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            RoutePointDto p = waypoints.get(i);
            if (i > 0) {
                coords.append(";");
            }
            coords.append(p.getLng()).append(",").append(p.getLat());
        }

        String url = UriComponentsBuilder
                .fromUriString(osrmBaseUrl + "/route/v1/driving/" + coords)
                .queryParam("overview", "full")
                .queryParam("geometries", "geojson")
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(body);

            if (!"Ok".equals(root.path("code").asText())) {
                log.info("OSRM 경로 없음: {}", root.path("code").asText());
                return null;
            }

            JsonNode routes = root.path("routes");
            if (!routes.isArray() || routes.isEmpty()) {
                return null;
            }

            JsonNode route = routes.get(0);
            JsonNode coordinates = route.path("geometry").path("coordinates");

            List<RoutePointDto> path = new ArrayList<>();
            for (JsonNode c : coordinates) {
                // GeoJSON은 [lng, lat] 순서
                double lng = c.get(0).asDouble();
                double lat = c.get(1).asDouble();
                path.add(new RoutePointDto(lat, lng));
            }

            if (path.isEmpty()) {
                return null;
            }

            long distance = Math.round(route.path("distance").asDouble(0));
            long duration = Math.round(route.path("duration").asDouble(0));
            return new RoutingResult(path, distance, duration);
        } catch (Exception e) {
            log.warn("OSRM 길찾기 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    public record RoutingResult(List<RoutePointDto> path, long distance, long duration) {
    }
}
