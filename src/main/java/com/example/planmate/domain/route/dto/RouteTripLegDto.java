package com.example.planmate.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "최적화된 방문 순서의 구간별 이동 정보")
public class RouteTripLegDto {
    @Schema(description = "구간 이동 거리(m)")
    private long distance;

    @Schema(description = "구간 소요 시간(초)")
    private long duration;
}
