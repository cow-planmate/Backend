package com.example.planmate.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 요청 데이터")
public class TransitRouteRequest {
    @Schema(description = "출발지 좌표")
    private RoutePointDto from;

    @Schema(description = "도착지 좌표")
    private RoutePointDto to;
}
