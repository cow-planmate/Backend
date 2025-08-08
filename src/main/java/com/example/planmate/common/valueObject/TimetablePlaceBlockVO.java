package com.example.planmate.common.valueObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private LocalTime startTime;
    private LocalTime endTime;
    @JsonProperty("xLocation")
    private Double xLocation;
    @JsonProperty("yLocation")
    private Double yLocation;
}
