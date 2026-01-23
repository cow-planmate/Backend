package com.example.planmate.common.valueObject;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceBlockVO {
    private int blockId;
    private int timeTableId;
    private int placeCategoryId;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private String photoUrl;
    private String placeId;
    private double xLocation;
    private double yLocation;
    private LocalTime blockStartTime;
    private LocalTime blockEndTime;
}
