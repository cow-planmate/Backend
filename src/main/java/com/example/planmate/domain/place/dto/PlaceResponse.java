package com.example.planmate.domain.place.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PlaceVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "장소 검색 응답 데이터")
public class PlaceResponse extends CommonResponse {
    @Schema(description = "검색된 장소 목록")
    private final List<PlaceVO> places;

    @Schema(description = "다음 페이지 조회를 위한 토큰 목록")
    private final List<NextPageTokenDTO> nextPageTokens;

    public PlaceResponse() {
        places = new ArrayList<>();
        nextPageTokens = new ArrayList<>();
    }

    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }

    public void addNextPageToken(List<NextPageTokenDTO> nextPageTokens) {
        this.nextPageTokens.addAll(nextPageTokens);
    }
}
