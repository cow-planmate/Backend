package com.example.planmate.dto;

import com.example.planmate.valueObject.PlaceVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class PlaceResponse extends CommonResponse{
    private List<PlaceVO> places;

    public PlaceResponse() {
        places = new ArrayList<>();
    }
    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }
}
