package com.example.planmate.common.valueObject;

import java.time.LocalTime;

import com.example.planmate.domain.webSocket.dto.LocalTime24Deserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TimetablePlaceBlockVO {
    private int timetableId;
    private Integer timetablePlaceBlockId;
    private Integer placeCategoryId;
    private String placeName;
    private Float placeRating;
    private String placeAddress;
    private String placeLink;
    private String placeId;
    private String date;
    @JsonProperty("startTime")
    @JsonAlias({ "blockStartTime" })    
    private LocalTime startTime;
    @JsonDeserialize(using = LocalTime24Deserializer.class)
    @JsonProperty("endTime")
    @JsonAlias({ "blockEndTime" })    
    private LocalTime endTime;
    @JsonProperty("xLocation")
    private Double xLocation;
    @JsonProperty("yLocation")
    private Double yLocation;
}
