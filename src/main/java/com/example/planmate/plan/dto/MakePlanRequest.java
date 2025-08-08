package com.example.planmate.plan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
public class MakePlanRequest {
    private String departure;
    private int travelId;
    @JsonProperty("transportation")
    private int transportationCategoryId;
    private List<LocalDate> dates;
    private int adultCount;
    private int childCount;
}
