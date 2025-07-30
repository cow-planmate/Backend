package com.example.planmate.wdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanResponse{
    private String planName;
    private String travel;
    private String departure;
    private Integer transportationCategoryId;
    private Integer adultCount;
    private Integer childCount;
}
