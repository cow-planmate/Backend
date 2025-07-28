package com.example.planmate.wdto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "object")
@JsonSubTypes({
        @JsonSubTypes.Type(value = WTimetableRequest.class, name = "timetable"),
        @JsonSubTypes.Type(value = WTimeTablePlaceBlockRequest.class, name = "timetablePlaceBlock")
})
@Getter
@Setter
public class WRequest {
    private String type;
    private String object;
}
