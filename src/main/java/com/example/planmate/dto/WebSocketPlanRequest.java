package com.example.planmate.dto;

import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketPlanRequest {
    private String type;
    private String object;
    private String planName;
    private String travel;
    private String departure;
    private Integer transportationCategoryId;
    private Integer adultCount;
    private Integer childCount;
    private TimetableVO timetableVO;
    private TimetablePlaceBlockVO timetablePlaceBlockVO;
}
