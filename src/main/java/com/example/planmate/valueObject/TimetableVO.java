package com.example.planmate.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
@Getter
@Setter
@AllArgsConstructor
public class TimetableVO {
    private int timetableId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
