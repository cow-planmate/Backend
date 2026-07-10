package com.example.planmate.common.externalAPI;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.route.dto.RoutePointDto;
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

            JsonNode paths = root.path("result").path("path");
            if (!paths.isArray() || paths.isEmpty()) {
                return TransitResult.unavailable("대중교통 경로를 찾을 수 없습니다");
            }

            // 첫 번째 경로가 최적 경로다.
            JsonNode best = paths.path(0);
            JsonNode info = best.path("info");
            return new TransitResult(
                    true,
                    null,
                    info.path("totalTime").asInt(0),
                    info.path("payment").asInt(0),
                    info.path("totalDistance").asInt(0),
                    info.path("busTransitCount").asInt(0),
                    info.path("subwayTransitCount").asInt(0),
                    best.path("pathType").asInt(0));
        } catch (Exception e) {
            log.warn("ODsay 대중교통 호출 실패: {}", e.getMessage());
            return null;
        }
    }

    public record TransitResult(boolean available, String message, Integer totalTime, Integer payment,
                                Integer totalDistance, Integer busTransitCount, Integer subwayTransitCount,
                                Integer pathType) {

        public static TransitResult unavailable(String message) {
            return new TransitResult(false, message, null, null, null, null, null, null);
        }
    }
}
