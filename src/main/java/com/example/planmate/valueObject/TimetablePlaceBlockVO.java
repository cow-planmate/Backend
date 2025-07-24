package com.example.planmate.valueObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimetablePlaceBlockVO {
    private int timetablePlaceBlockId;
    @JsonProperty("placeCategoryId")
    private int placeCategoryId;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private String date;
    private LocalTime startTime;
    private LocalTime endTime;
    @JsonProperty("xLocation")
    private double xLocation;
    @JsonProperty("yLocation")
    private double yLocation;
}
