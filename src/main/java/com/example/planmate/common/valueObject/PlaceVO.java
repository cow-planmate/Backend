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
}
