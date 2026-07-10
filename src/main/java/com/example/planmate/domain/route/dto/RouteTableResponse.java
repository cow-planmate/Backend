package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 쌍별 소요시간/거리 매트릭스 응답 데이터")
public class RouteTableResponse {
    @Schema(description = "좌표 쌍별 소요 시간(초) NxN 매트릭스 (도달 불가 구간은 null)")
    private List<List<Double>> durations;

    @Schema(description = "좌표 쌍별 이동 거리(m) NxN 매트릭스 (도달 불가 구간은 null)")
    private List<List<Double>> distances;

    @Schema(description = "적용된 이동 수단 프로필", example = "driving")
    private String profile;
}
