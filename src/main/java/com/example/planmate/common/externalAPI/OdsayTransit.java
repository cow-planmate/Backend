package com.example.planmate.common.externalAPI;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.route.dto.RoutePointDto;
import com.example.planmate.domain.route.dto.TransitLaneDto;
import com.example.planmate.domain.route.dto.TransitRouteOptionDto;
import com.example.planmate.domain.route.dto.TransitStepDto;
import com.example.planmate.domain.route.dto.TransitStopDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * ODsay API로 두 지점 간 대중교통 경로(최적 경로 1건)를 조회한다.
 * api.odsay.key가 비어 있으면 API를 호출하지 않고 사용 불가 결과를 반환한다.
 */
@Component
@Slf4j
public class OdsayTransit {

    @Value("${api.odsay.key:}")
    private String odsayApiKey;

    @Value("${api.odsay.base-url:https://api.odsay.com/v1/api}")
    private String odsayBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 두 지점 간 대중교통 최적 경로를 조회한다.
     * 호출 자체가 실패하면 null을 반환한다(호출 측에서 사용 불가 응답으로 대체).
     */
    public TransitResult getTransitPath(RoutePointDto from, RoutePointDto to) {
        if (from == null || to == null) {
            return null;
        }
        if (odsayApiKey == null || odsayApiKey.isBlank()) {
            return TransitResult.unavailable("ODsay API 키가 설정되지 않았습니다");
        }

        // ODsay 키에는 '+', '=' 등이 포함되므로 정확히 한 번만 URL 인코딩한다.
        String encodedKey = URLEncoder.encode(odsayApiKey, StandardCharsets.UTF_8);
        String url = odsayBaseUrl + "/searchPubTransPathT"
                + "?SX=" + from.getLng()
                + "&SY=" + from.getLat()
                + "&EX=" + to.getLng()
                + "&EY=" + to.getLat()
                + "&apiKey=" + encodedKey;

        try {
            // RestTemplate이 문자열 URL을 다시 인코딩하지 않도록 URI로 감싸 호출한다(이중 인코딩 방지).
            String body = restTemplate.getForObject(URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(body);

            JsonNode error = root.path("error");
            if (!error.isMissingNode() && !error.isNull()) {
                String message = error.isArray()
                        ? error.path(0).path("message").asText("ODsay API 오류")
                        : error.path("message").asText("ODsay API 오류");
                log.info("ODsay 오류 응답: {}", message);
                return TransitResult.unavailable(message);
            }

            JsonNode result = root.path("result");
            JsonNode paths = result.path("path");
            if (!paths.isArray() || paths.isEmpty()) {
                return TransitResult.unavailable("대중교통 경로를 찾을 수 없습니다");
            }

            // 결과 레벨의 전체 경로 개수(우리가 담는 ≤10건과 무관한 실제 총합).
            Integer busCount = intOrDefault(result.path("busCount"), 0);
            Integer subwayCount = intOrDefault(result.path("subwayCount"), 0);
            Integer subwayBusCount = intOrDefault(result.path("subwayBusCount"), 0);

            // ODsay는 최적경로순으로 정렬해 주므로 순서를 유지한 채 최대 10건까지 담는다.
            List<TransitRouteOptionDto> routes = new ArrayList<>();
            for (JsonNode path : paths) {
                if (routes.size() >= 10) {
                    break;
                }
                JsonNode info = path.path("info");
                routes.add(new TransitRouteOptionDto(
                        intOrDefault(path.path("pathType"), 0),
                        intOrDefault(info.path("totalTime"), 0),
                        intOrDefault(info.path("payment"), 0),
                        intOrDefault(info.path("totalWalk"), 0),
                        intOrDefault(info.path("totalDistance"), 0),
                        intOrDefault(info.path("busTransitCount"), 0),
                        intOrDefault(info.path("subwayTransitCount"), 0),
                        textOrNull(info.path("firstStartStation")),
                        textOrNull(info.path("lastEndStation")),
                        parseSteps(path.path("subPath")),
                        textOrNull(info.path("mapObj"))));
            }
            return new TransitResult(true, null, routes, busCount, subwayCount, subwayBusCount);
        } catch (Exception e) {
            log.warn("ODsay 대중교통 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * subPath 배열을 구간별 상세 스텝으로 파싱한다. 누락 필드는 null로 남긴다.
     */
    private List<TransitStepDto> parseSteps(JsonNode subPath) {
        List<TransitStepDto> steps = new ArrayList<>();
        if (subPath == null || !subPath.isArray()) {
            return steps;
        }

        for (JsonNode sub : subPath) {
            Integer trafficType = intOrNull(sub.path("trafficType"));
            Integer sectionTime = intOrNull(sub.path("sectionTime"));
            Integer distance = intOrNull(sub.path("distance"));
            Integer stationCount = intOrNull(sub.path("stationCount"));
            Integer intervalTime = intOrNull(sub.path("intervalTime"));
            String startName = textOrNull(sub.path("startName"));
            String endName = textOrNull(sub.path("endName"));
            String wayName = textOrNull(sub.path("way"));
            String startExitNo = textOrNull(sub.path("startExitNo"));
            String endExitNo = textOrNull(sub.path("endExitNo"));
            String startArsID = textOrNull(sub.path("startArsID"));
            String endArsID = textOrNull(sub.path("endArsID"));

            String laneName = null;
            Integer busType = null;
            Integer subwayCode = null;
            JsonNode lane = sub.path("lane").path(0);
            if (!lane.isMissingNode() && !lane.isNull()) {
                if (trafficType != null && trafficType == 2) {
                    laneName = textOrNull(lane.path("busNo"));
                    busType = intOrNull(lane.path("type"));
                } else if (trafficType != null && trafficType == 1) {
                    laneName = textOrNull(lane.path("name"));
                    subwayCode = intOrNull(lane.path("subwayCode"));
                }
            }

            List<TransitStopDto> passStops = parsePassStops(sub.path("passStopList").path("stations"));

            steps.add(new TransitStepDto(trafficType, sectionTime, distance, stationCount, laneName,
                    busType, subwayCode, startName, endName, wayName, startExitNo, endExitNo,
                    intervalTime, startArsID, endArsID, passStops));
        }
        return steps;
    }

    /**
     * passStopList.stations 배열을 경유 정류장 목록으로 파싱한다. 없으면 빈 목록.
     * x/y는 ODsay가 문자열로 주므로 안전하게 Double로 변환한다(비었으면 null).
     */
    private List<TransitStopDto> parsePassStops(JsonNode stations) {
        List<TransitStopDto> stops = new ArrayList<>();
        if (stations == null || !stations.isArray()) {
            return stops;
        }
        for (JsonNode station : stations) {
            stops.add(new TransitStopDto(
                    intOrNull(station.path("index")),
                    textOrNull(station.path("stationName")),
                    doubleOrNull(station.path("x")),
                    doubleOrNull(station.path("y")),
                    textOrNull(station.path("arsID"))));
        }
        return stops;
    }

    /**
     * 선택한 경로의 mapObj로 loadLane을 호출해 노선별 폴리라인을 조회한다.
     * 키가 비어 있으면 null, 오류/없음이면 빈 목록을 반환한다.
     */
    public TransitLaneResult getLane(String mapObj) {
        if (odsayApiKey == null || odsayApiKey.isBlank()) {
            return null;
        }
        if (mapObj == null || mapObj.isBlank()) {
            return new TransitLaneResult(new ArrayList<>());
        }

        // getTransitPath와 동일하게 값 부분만 정확히 한 번 URL 인코딩한다(이중 인코딩 방지).
        String encodedKey = URLEncoder.encode(odsayApiKey, StandardCharsets.UTF_8);
        String encodedMapObject = URLEncoder.encode("0:0@" + mapObj, StandardCharsets.UTF_8);
        String url = odsayBaseUrl + "/loadLane"
                + "?mapObject=" + encodedMapObject
                + "&apiKey=" + encodedKey;

        try {
            String body = restTemplate.getForObject(URI.create(url), String.class);
            JsonNode root = objectMapper.readTree(body);

            JsonNode error = root.path("error");
            if (!error.isMissingNode() && !error.isNull()) {
                return new TransitLaneResult(new ArrayList<>());
            }

            JsonNode lanes = root.path("result").path("lane");
            List<TransitLaneDto> laneDtos = new ArrayList<>();
            if (lanes.isArray()) {
                for (JsonNode lane : lanes) {
                    List<RoutePointDto> path = new ArrayList<>();
                    JsonNode sections = lane.path("section");
                    if (sections.isArray()) {
                        for (JsonNode section : sections) {
                            JsonNode graphPos = section.path("graphPos");
                            if (graphPos.isArray()) {
                                for (JsonNode point : graphPos) {
                                    Double lng = doubleOrNull(point.path("x"));
                                    Double lat = doubleOrNull(point.path("y"));
                                    if (lat != null && lng != null) {
                                        path.add(new RoutePointDto(lat, lng));
                                    }
                                }
                            }
                        }
                    }
                    laneDtos.add(new TransitLaneDto(
                            intOrNull(lane.path("class")),
                            intOrNull(lane.path("type")),
                            path));
                }
            }
            return new TransitLaneResult(laneDtos);
        } catch (Exception e) {
            log.warn("ODsay loadLane 호출 실패: {}", e.getMessage());
            return new TransitLaneResult(new ArrayList<>());
        }
    }

    private static Integer intOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) {
            return null;
        }
        return node.asInt();
    }

    private static Integer intOrDefault(JsonNode node, int defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) {
            return defaultValue;
        }
        return node.asInt();
    }

    /**
     * ODsay가 문자열/숫자로 주는 좌표를 안전하게 Double로 변환한다.
     * 없음/빈값/"null"이면 null.
     */
    private static Double doubleOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        String text = node.asText(null);
        if (text == null || text.isBlank() || "null".equals(text)) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String text = node.asText(null);
        // ODsay는 값이 없을 때 문자열 "null"을 반환하기도 한다(예: door, 출구 없는 구간).
        if (text == null || text.isBlank() || "null".equals(text)) {
            return null;
        }
        return text;
    }

    public record TransitResult(boolean available, String message, List<TransitRouteOptionDto> routes,
            Integer busCount, Integer subwayCount, Integer subwayBusCount) {

        public static TransitResult unavailable(String message) {
            return new TransitResult(false, message, new ArrayList<>(), 0, 0, 0);
        }
    }

    public record TransitLaneResult(List<TransitLaneDto> lanes) {
    }
}
