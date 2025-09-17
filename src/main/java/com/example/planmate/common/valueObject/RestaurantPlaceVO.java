package com.example.planmate.common.valueObject;

public class RestaurantPlaceVO extends PlaceVO {
    public RestaurantPlaceVO(String placeId, int categoryId, String placeLink, String name, String formatted_address, float rating, double xLocation, double yLocation, String iconUrl) {
        super(placeId, categoryId, placeLink, name, formatted_address, rating, xLocation, yLocation, iconUrl);
    }
}
