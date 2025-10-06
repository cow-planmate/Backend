package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "timeTable", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<TimeTablePlaceBlock> placeBlocks = new ArrayList<>();

    public void changeId(Integer newId) {
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
