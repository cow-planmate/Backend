package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
@AllArgsConstructor
public class TimetableVO {
    private int timetableId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
