package com.example.planmate.common.valueObject;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimetableVO {
    private Integer timeTableId;
    private LocalDate date;
    private LocalTime timeTableStartTime;
    private LocalTime timeTableEndTime;
}
