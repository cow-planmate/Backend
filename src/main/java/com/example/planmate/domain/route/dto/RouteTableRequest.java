package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "장소 쌍별 소요시간/거리 매트릭스 요청 데이터")
public class RouteTableRequest {
    @Schema(description = "매트릭스를 계산할 좌표 목록")
    private List<RoutePointDto> waypoints;

    @Schema(description = "이동 수단 프로필 (driving | foot)", example = "driving", defaultValue = "driving")
    private String profile;
}
