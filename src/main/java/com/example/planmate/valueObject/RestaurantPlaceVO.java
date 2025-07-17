package com.example.planmate.valueObject;

public class RestaurantPlaceVO extends PlaceVO {
    public RestaurantPlaceVO(String placeId, String url, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        super(placeId, url, name, formatted_address, rating, xLocation, yLocation, iconUrl);
    }
}
