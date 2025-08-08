package com.example.planmate.domain.webSocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanRequest{
    private String planName;
    private Integer travelId;
    private String departure;
    private Integer transportationCategoryId;
    private Integer adultCount;
    private Integer childCount;
}
