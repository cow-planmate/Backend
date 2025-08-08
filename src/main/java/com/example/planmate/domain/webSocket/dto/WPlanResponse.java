package com.example.planmate.domain.webSocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanResponse{
    private String planName;
    private String travelCategoryName;
    private Integer travelId;
    private String travelName;
    private String departure;
    private Integer transportationCategoryId;
    private Integer adultCount;
    private Integer childCount;
}
