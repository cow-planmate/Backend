package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 옵션 (여러 경로 중 하나)")
public class TransitRouteOptionDto {
    @Schema(description = "경로 유형 (1=지하철, 2=버스, 3=버스+지하철)")
    private Integer pathType;

    @Schema(description = "총 소요 시간(분)")
    private Integer totalTime;

    @Schema(description = "요금(원)")
    private Integer payment;

    @Schema(description = "도보 총거리(m)")
    private Integer totalWalk;

    @Schema(description = "총 이동 거리(m)")
    private Integer totalDistance;

    @Schema(description = "버스 환승 횟수")
    private Integer busTransitCount;

    @Schema(description = "지하철 환승 횟수")
    private Integer subwayTransitCount;

    @Schema(description = "최초 승차 정류장/역 이름")
    private String firstStartStation;

    @Schema(description = "최종 하차 정류장/역 이름")
    private String lastEndStation;

    @Schema(description = "구간별 상세 경로 (노선/승하차 정류장 수준)")
    private List<TransitStepDto> steps;

    @Schema(description = "이 경로의 지도 폴리라인 조회용 mapObj (ODsay path.info.mapObj)")
    private String mapObj;
}
