package com.example.planmate.wdto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanRequest extends WRequest {
    private String planName;
    private Integer travelId;
    private String departure;
    private Integer transportationCategoryId;
    private Integer adultCount;
    private Integer childCount;
}
