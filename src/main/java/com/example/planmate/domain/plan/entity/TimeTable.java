package com.example.planmate.domain.plan.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "time_table")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timeTableId;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime timeTableStartTime;

    private LocalTime timeTableEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
    public void timeTableAddNotId(TimeTable timeTable) {
        timeTable.setDate(date);
        timeTable.setTimeTableStartTime(timeTableStartTime);
        timeTable.setTimeTableEndTime(timeTableEndTime);
    }

    public void changeId(Integer newId) {
        if (newId == null || newId <= 0) {
            throw new IllegalArgumentException("ID는 null이거나 0 이하일 수 없습니다.");
        }
        this.timeTableId = newId;
    }

    public void changeDate(LocalDate newDate) {
        if (newDate == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        this.date = newDate;
    }

    public void changeTime(LocalTime start, LocalTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        this.timeTableStartTime = start;
        this.timeTableEndTime = end;
    }
}
