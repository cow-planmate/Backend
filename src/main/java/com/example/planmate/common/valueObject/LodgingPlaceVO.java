package com.example.planmate.common.valueObject;

public class LodgingPlaceVO extends PlaceVO {
    public LodgingPlaceVO(String placeId, int categoryId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        super(placeId, categoryId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
    }
}
