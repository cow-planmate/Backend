package com.example.planmate.common.externalAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @Value("${api.osrm.foot-base-url:https://routing.openstreetmap.de/routed-foot}")
    private String osrmFootBaseUrl;

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

    /**
     * 좌표 쌍별 소요 시간(초)/거리(m) NxN 매트릭스를 조회한다.
     * 도달 불가 구간은 null 항목으로 유지되며, 실패하면 null을 반환한다.
     */
    public TableResult getTable(List<RoutePointDto> waypoints, String profile) {
        if (waypoints == null || waypoints.size() < 2 || waypoints.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        String normalized = normalizeProfile(profile);
        String url = UriComponentsBuilder
                .fromUriString(resolveBaseUrl(normalized) + "/table/v1/" + normalized + "/" + buildCoords(waypoints))
                .queryParam("annotations", "duration,distance")
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(body);

            if (!"Ok".equals(root.path("code").asText())) {
                log.info("OSRM 테이블 조회 실패: {}", root.path("code").asText());
                return null;
            }

            List<List<Double>> durations = parseMatrix(root.path("durations"));
            List<List<Double>> distances = parseMatrix(root.path("distances"));
            if (durations == null || distances == null) {
                return null;
            }
            return new TableResult(durations, distances);
        } catch (Exception e) {
            log.warn("OSRM 테이블 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 방문 순서 최적화(외판원) 경로를 조회한다. 첫 좌표를 출발지로 고정한다.
     * 실패하면 null을 반환한다.
     */
    public TripResult getTrip(List<RoutePointDto> waypoints, String profile, boolean roundtrip) {
        if (waypoints == null || waypoints.size() < 2 || waypoints.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        String normalized = normalizeProfile(profile);
        String url = UriComponentsBuilder
                .fromUriString(resolveBaseUrl(normalized) + "/trip/v1/" + normalized + "/" + buildCoords(waypoints))
                .queryParam("source", "first")
                .queryParam("roundtrip", roundtrip)
                .queryParam("overview", "false")
                .toUriString();

        try {
            String body = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(body);

            if (!"Ok".equals(root.path("code").asText())) {
                log.info("OSRM 방문 순서 최적화 실패: {}", root.path("code").asText());
                return null;
            }

            JsonNode trips = root.path("trips");
            if (!trips.isArray() || trips.isEmpty()) {
                return null;
            }
            JsonNode trip = trips.get(0);

            // waypoints[i].waypoint_index = 입력 i번 좌표의 방문 순번 → 방문 순서대로 입력 인덱스를 재배열한다.
            JsonNode osrmWaypoints = root.path("waypoints");
            int n = waypoints.size();
            if (!osrmWaypoints.isArray() || osrmWaypoints.size() != n) {
                return null;
            }
            Integer[] order = new Integer[n];
            for (int i = 0; i < n; i++) {
                int position = osrmWaypoints.path(i).path("waypoint_index").asInt(-1);
                if (position < 0 || position >= n || order[position] != null) {
                    return null;
                }
                order[position] = i;
            }

            List<TripLeg> legs = new ArrayList<>();
            for (JsonNode leg : trip.path("legs")) {
                legs.add(new TripLeg(
                        Math.round(leg.path("distance").asDouble(0)),
                        Math.round(leg.path("duration").asDouble(0))));
            }

            long distance = Math.round(trip.path("distance").asDouble(0));
            long duration = Math.round(trip.path("duration").asDouble(0));
            return new TripResult(new ArrayList<>(Arrays.asList(order)), distance, duration, legs);
        } catch (Exception e) {
            log.warn("OSRM 방문 순서 최적화 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 프로필 문자열을 지원 값("driving" | "foot")으로 정규화한다. 알 수 없는 값은 "driving"으로 처리한다.
     */
    public static String normalizeProfile(String profile) {
        if (profile != null && "foot".equalsIgnoreCase(profile.trim())) {
            return "foot";
        }
        return "driving";
    }

    /**
     * 프로필에 맞는 OSRM 서버 주소를 고른다.
     */
    private String resolveBaseUrl(String normalizedProfile) {
        return "foot".equals(normalizedProfile) ? osrmFootBaseUrl : osrmBaseUrl;
    }

    /**
     * OSRM 좌표 문자열(lng,lat;lng,lat;...)을 만든다.
     */
    private String buildCoords(List<RoutePointDto> waypoints) {
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < waypoints.size(); i++) {
            RoutePointDto p = waypoints.get(i);
            if (i > 0) {
                coords.append(";");
            }
            coords.append(p.getLng()).append(",").append(p.getLat());
        }
        return coords.toString();
    }

    /**
     * OSRM NxN 매트릭스 응답을 null 항목을 보존하며 파싱한다.
     */
    private List<List<Double>> parseMatrix(JsonNode matrix) {
        if (!matrix.isArray()) {
            return null;
        }
        List<List<Double>> result = new ArrayList<>();
        for (JsonNode row : matrix) {
            List<Double> parsedRow = new ArrayList<>();
            for (JsonNode cell : row) {
                parsedRow.add(cell.isNull() ? null : cell.asDouble());
            }
            result.add(parsedRow);
        }
        return result;
    }

    public record RoutingResult(List<RoutePointDto> path, long distance, long duration) {
    }

    public record TableResult(List<List<Double>> durations, List<List<Double>> distances) {
    }

    public record TripResult(List<Integer> visitOrder, long totalDistance, long totalDuration, List<TripLeg> legs) {
    }

    public record TripLeg(long distance, long duration) {
    }
}
