package com.example.planmate.valueObject;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class TimetablePlaceBlockVO {
    private int timetableId;
    private Integer timetablePlaceBlockId;
    private Integer placeCategoryId;
    private String placeName;
    private String placeTheme;
    private Float placeRating;
    private String placeAddress;
    private String placeLink;
    private String date;
    private LocalTime blockStartTime;
    private LocalTime blockEndTime;
    private Double xLocation;
    private Double yLocation;
}
