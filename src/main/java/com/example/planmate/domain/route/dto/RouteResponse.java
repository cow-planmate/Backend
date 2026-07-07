package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길찾기 응답 데이터")
public class RouteResponse {
    @Schema(description = "도로를 따라가는 경로 좌표 목록")
    private List<RoutePointDto> path;

    @Schema(description = "총 이동 거리(m)")
    private long distance;

    @Schema(description = "총 소요 시간(초)")
    private long duration;
}
