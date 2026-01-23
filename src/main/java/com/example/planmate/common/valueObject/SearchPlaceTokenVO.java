package com.example.planmate.common.valueObject;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
@Getter
public class SearchPlaceTokenVO {

    private List<PlaceVO> places;
    private List<String> nextPageTokens;

    public SearchPlaceTokenVO() {
        places = new ArrayList<>();
        nextPageTokens = new ArrayList<>();
    }
    public void addNextPageToken(String nextPageToken) {
        nextPageTokens.add(nextPageToken);
    }
    public void addPlace(PlaceVO place) {
        places.add(place);
    }
}
