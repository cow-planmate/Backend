package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길찾기 요청 데이터")
public class RouteRequest {
    @Schema(description = "방문 순서대로 나열된 경유지 좌표 목록 (출발지 ~ 목적지)")
    private List<RoutePointDto> waypoints;
}
