package com.example.planmate.valueObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimetablePlaceBlockVO {
    private int timetablePlaceBlockId;
    private String placeCategory;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private String date;
    private String startTime;
    private String endTime;
    private double xLocation;
    private double yLocation;
}
