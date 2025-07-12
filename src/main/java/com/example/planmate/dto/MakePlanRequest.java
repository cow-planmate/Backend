package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
@Getter
@Setter
public class MakePlanRequest {
    private String departure;
    private int travelId;
    private List<LocalDate> dates;
    private int adultCount;
    private int childCount;
}
