package com.example.planmate.domain.route.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 폴리라인 응답 (loadLane)")
public class TransitLaneResponse {
    @Schema(description = "노선별 폴리라인 목록 (조회 불가/없음 시 빈 목록)")
    private List<TransitLaneDto> lanes;
}
