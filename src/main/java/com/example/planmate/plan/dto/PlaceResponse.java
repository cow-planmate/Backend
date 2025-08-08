package com.example.planmate.plan.dto;

import com.example.planmate.dto.CommonResponse;
import com.example.planmate.valueObject.PlaceVO;
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
