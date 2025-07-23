package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceBlockVO {
    private int placeCategory;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private double xLocation;
    private double yLocation;
}
