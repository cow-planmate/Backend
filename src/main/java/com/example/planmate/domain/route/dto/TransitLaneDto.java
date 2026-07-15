package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로의 지도 폴리라인 한 구간(노선)")
public class TransitLaneDto {
    @Schema(description = "교통수단 종류 (ODsay class; 1=버스, 2=지하철)")
    private Integer trafficClass;

    @Schema(description = "노선 종류 코드 (버스=busType, 지하철=subwayCode)")
    private Integer type;

    @Schema(description = "폴리라인 좌표 목록 (모든 section의 graphPos를 순서대로 이어붙임)")
    private List<RoutePointDto> path;
}
