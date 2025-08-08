package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PlaceVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class PlaceResponse extends CommonResponse {
    private List<PlaceVO> places;

    public PlaceResponse() {
        places = new ArrayList<>();
    }
    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }
}
