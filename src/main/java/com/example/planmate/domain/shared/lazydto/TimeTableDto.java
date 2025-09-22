package com.example.planmate.domain.shared.lazydto;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.Plan;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeTableDto(
        Integer timeTableId,
        LocalDate date,
        LocalTime timeTableStartTime,
        LocalTime timeTableEndTime,
        Integer planId
) {
    public static TimeTableDto fromEntity(TimeTable timeTable) {
        return new TimeTableDto(
                timeTable.getTimeTableId(),
                timeTable.getDate(),
                timeTable.getTimeTableStartTime(),
                timeTable.getTimeTableEndTime(),
                timeTable.getPlan().getPlanId()
        );
    }

    public TimeTable toEntity(Plan plan) {
        return TimeTable.builder()
                .timeTableId(this.timeTableId)
                .date(this.date)
                .timeTableStartTime(this.timeTableStartTime)
                .timeTableEndTime(this.timeTableEndTime)
                .plan(plan)
                .build();
    }
}