package com.example.planmate.common.valueObject;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class SearchPlaceTokenVO {

    private List<SearchPlaceVO> places;
    private List<String> nextPageTokens;

    public SearchPlaceTokenVO() {
        places = new ArrayList<>();
        nextPageTokens = new ArrayList<>();
    }
    public void addNextPageToken(String nextPageToken) {
        nextPageTokens.add(nextPageToken);
    }
    public void addPlace(SearchPlaceVO place) {
        places.add(place);
    }
}
