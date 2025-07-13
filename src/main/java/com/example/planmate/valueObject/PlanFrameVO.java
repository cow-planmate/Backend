package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlanFrameVO {
    private int planId;
    private String planName;
    private String departure;
    private String travel;
    private int adultCount;
    private int childCount;
    private int transportation;
}
