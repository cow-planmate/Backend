package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlaceVO {
    private String placeId;
    private int categoryId;
    private String url;
    private String name;
    private String formatted_address;
    private float rating;
    private String photoUrl;
    private double xLocation;
    private double yLocation;
    private String iconUrl;
    private String photoReference; 

    public PlaceVO(String placeId, int categoryId, String url, String name, String formatted_address, float rating, String photoUrl, double xLocation, double yLocation, String iconUrl) {
        this.placeId = placeId;
        this.categoryId = categoryId;
        this.url = url;
        this.name = name;
        this.formatted_address = formatted_address;
        this.rating = rating;
        this.photoUrl = photoUrl;
        this.xLocation = xLocation;
        this.yLocation = yLocation;
        this.iconUrl = iconUrl;
    }
}

