package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PlaceVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class PlaceResponse extends CommonResponse {
    private final List<PlaceVO> places;
    private final List<String> nextPageTokens;

    public PlaceResponse() {
        places = new ArrayList<>();
        nextPageTokens = new ArrayList<>();
    }
    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }
    public void addNextPageToken(List<String> nextPageTokens){
        this.nextPageTokens.addAll(nextPageTokens);
    }
}
