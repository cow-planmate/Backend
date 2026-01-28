package com.example.planmate.domain.travel.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.TravelVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
@Getter
@Schema(description = "여행지 목록 조회 응답 데이터")
public class GetTravelResponse extends CommonResponse {
    @Schema(description = "여행지 목록")
    private final List<TravelVO> travels;

    public GetTravelResponse() {
        this.travels = new ArrayList<>();
    }

    public void addTravel(int travelId, String travelName,
                          int travelCategoryId, String travelCategoryName) {
        this.travels.add(new TravelVO(travelId, travelName, travelCategoryId, travelCategoryName));
    }
}
