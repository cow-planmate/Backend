package com.example.planmate.domain.shared.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;

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

    /**
     * ID만 변경된 새로운 TimeTableDto 객체를 생성합니다.
     * @param newTimeTableId 새로운 타임테이블 ID
     * @return ID가 변경된 새로운 DTO 객체
     */
    public TimeTableDto withTimeTableId(Integer newTimeTableId) {
        return new TimeTableDto(
                newTimeTableId,
                this.date,
                this.timeTableStartTime,
                this.timeTableEndTime,
                this.planId
        );
    }
}