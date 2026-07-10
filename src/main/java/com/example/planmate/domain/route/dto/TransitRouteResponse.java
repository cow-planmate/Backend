package com.example.planmate.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 응답 데이터")
public class TransitRouteResponse {
    @Schema(description = "대중교통 경로 조회 가능 여부")
    private boolean available;

    @Schema(description = "조회 불가 사유 (성공 시 null)")
    private String message;

    @Schema(description = "총 소요 시간(분)")
    private Integer totalTime;

    @Schema(description = "요금(원)")
    private Integer payment;

    @Schema(description = "총 이동 거리(m)")
    private Integer totalDistance;

    @Schema(description = "버스 환승 횟수")
    private Integer busTransitCount;

    @Schema(description = "지하철 환승 횟수")
    private Integer subwayTransitCount;

    @Schema(description = "경로 유형 (1=지하철, 2=버스, 3=혼합)")
    private Integer pathType;
}
