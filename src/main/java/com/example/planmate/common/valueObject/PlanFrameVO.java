package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlanFrameVO {
    private int planId;
    private String planName;
    private String departure;
    private String travelCategoryName;
    private int travelId;
    private String travelName;
    private int adultCount;
    private int childCount;
    private int transportationCategoryId;
}
