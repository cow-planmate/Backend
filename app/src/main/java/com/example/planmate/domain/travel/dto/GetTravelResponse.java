package com.example.planmate.domain.travel.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.TravelVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class GetTravelResponse extends CommonResponse {
    private final List<TravelVO> travels;

    public GetTravelResponse() {
        this.travels = new ArrayList<>();
    }

    public void addTravel(int travelId, String travelName,
                          int travelCategoryId, String travelCategoryName) {
        this.travels.add(new TravelVO(travelId, travelName, travelCategoryId, travelCategoryName));
    }
}
