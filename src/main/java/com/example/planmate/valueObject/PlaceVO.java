package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class PlaceVO {
    private String placeId;
    private String url;
    private String name;
    private String formatted_address;
    private float rating;
    private double xLocation;
    private double yLocation;
}
