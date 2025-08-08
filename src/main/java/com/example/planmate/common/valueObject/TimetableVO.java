package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimetableVO {
    private Integer timetableId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
