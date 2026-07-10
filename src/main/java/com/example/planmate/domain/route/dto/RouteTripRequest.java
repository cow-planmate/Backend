package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "방문 순서 최적화 요청 데이터")
public class RouteTripRequest {
    @Schema(description = "방문할 좌표 목록 (첫 좌표가 출발지로 고정)")
    private List<RoutePointDto> waypoints;

    @Schema(description = "이동 수단 프로필 (driving | foot)", example = "driving", defaultValue = "driving")
    private String profile;

    @Schema(description = "출발지로 되돌아오는 왕복 여부", example = "false", defaultValue = "false")
    private boolean roundtrip;
}
