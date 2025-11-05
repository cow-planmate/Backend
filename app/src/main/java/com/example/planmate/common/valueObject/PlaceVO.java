package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class PlaceVO {
    private String placeId;
    private int categoryId;
    private String url;
    private String name;
    private String formatted_address;
    private float rating;
    private double xLocation;
    private double yLocation;
    private String iconUrl;
}
