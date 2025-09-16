package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceBlockVO {
    private int blockId;
    private int timeTableId;
    private int placeCategory;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private String placeId;
    private double xLocation;
    private double yLocation;
    private LocalTime startTime;
    private LocalTime endTime;
}
