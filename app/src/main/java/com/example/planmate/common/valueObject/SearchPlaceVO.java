package com.example.planmate.common.valueObject;

public class SearchPlaceVO extends PlaceVO {
    public SearchPlaceVO(String placeId, int categoryId, String placeLink, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        super(placeId, categoryId, placeLink, name, formatted_address, rating, xLocation, yLocation, iconUrl);
    }
}
