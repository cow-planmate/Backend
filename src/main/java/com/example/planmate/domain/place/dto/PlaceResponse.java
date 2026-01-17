package com.example.planmate.domain.place.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.NextPageTokenVO;
import com.example.planmate.common.valueObject.PlaceVO;

import lombok.Getter;

@Getter
public class PlaceResponse extends CommonResponse {
    private final List<PlaceVO> places;
    private final List<NextPageTokenVO> nextPageTokens;

    public PlaceResponse() {
        places = new ArrayList<>();
        nextPageTokens = new ArrayList<>();
    }

    public void addPlace(List<? extends PlaceVO> places) {
        this.places.addAll(places);
    }

    public void addNextPageToken(List<NextPageTokenVO> nextPageTokens) {
        this.nextPageTokens.addAll(nextPageTokens);
    }
}
