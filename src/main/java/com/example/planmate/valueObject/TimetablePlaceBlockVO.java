package com.example.planmate.valueObject;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

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
    private LocalTime startTime;
    private LocalTime endTime;
    private Double xLocation;
    private Double yLocation;
}
