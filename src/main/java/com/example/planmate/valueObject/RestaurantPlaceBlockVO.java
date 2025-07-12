package com.example.planmate.valueObject;

public class RestaurantPlaceBlockVO extends PlaceBlockVO {
    public RestaurantPlaceBlockVO(int placeCategory, String placeName, String placeTheme, float placeRating, String placeAddress, String placeLink, double xLocation, double yLocation) {
        super(placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, xLocation, yLocation);
    }
}
