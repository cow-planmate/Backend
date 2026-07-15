package com.example.planmate.domain.route.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대중교통 경로 폴리라인 요청 (searchPubTransPathT의 path.info.mapObj)")
public class TransitLaneRequest {
    @Schema(description = "폴리라인을 조회할 경로의 mapObj 문자열")
    private String mapObj;
}
