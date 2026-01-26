package com.example.planmate.domain.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "장소 검색 요청 데이터")
public class SearchPlaceRequest {
    @Schema(description = "검색어", example = "경복궁")
    private String query;
}
