package com.example.planmate.common.valueObject;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimetablePlaceBlockVO {
    private int timeTableId;
    private Integer timetablePlaceBlockId;
    private Integer placeCategoryId;
    private String placeName;
    private Float placeRating;
    private String placeAddress;
    private String placeLink;
    private String photoUrl;
    @JsonProperty("placeId")
    @JsonAlias({ "placePhotoId" })
    private String placeId;
    private String date;
    @JsonProperty("startTime")
    @JsonAlias({ "blockStartTime" })    
    private LocalTime startTime;
    @JsonProperty("endTime")
    @JsonAlias({ "blockEndTime" })    
    private LocalTime endTime;
    @JsonProperty("xLocation")
    private Double xLocation;
    @JsonProperty("yLocation")
    private Double yLocation;
}
