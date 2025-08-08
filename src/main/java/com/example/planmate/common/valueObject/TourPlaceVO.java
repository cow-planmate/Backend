package com.example.planmate.common.valueObject;

public class TourPlaceVO extends PlaceVO {
    public TourPlaceVO(String placeId, int catrgoryId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        super(placeId, catrgoryId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
    }
}
