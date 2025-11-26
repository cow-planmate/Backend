package com.example.planmate.common.valueObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sharedsync.shared.util.LocalTime24Deserializer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

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
    private LocalTime startTime;
    @JsonDeserialize(using = LocalTime24Deserializer.class)
    private LocalTime endTime;
    @JsonProperty("xLocation")
    private Double xLocation;
    @JsonProperty("yLocation")
    private Double yLocation;
}
