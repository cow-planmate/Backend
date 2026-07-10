package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "방문 순서 최적화 응답 데이터")
public class RouteTripResponse {
    @Schema(description = "방문 순서대로 나열한 입력 좌표 인덱스 목록", example = "[0, 2, 1, 3]")
    private List<Integer> visitOrder;

    @Schema(description = "총 이동 거리(m)")
    private long totalDistance;

    @Schema(description = "총 소요 시간(초)")
    private long totalDuration;

    @Schema(description = "방문 순서의 구간별 이동 정보 목록")
    private List<RouteTripLegDto> legs;
}
