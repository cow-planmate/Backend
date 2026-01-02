package com.example.planmate.domain.plan.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.sharedsync.shared.annotation.CacheEntity;
import com.sharedsync.shared.annotation.CacheId;
import com.sharedsync.shared.annotation.ParentId;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "time_table")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@CacheEntity
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @CacheId
    private Integer timeTableId;

    @Column(nullable = false)
    private LocalDate date;

    private LocalTime timeTableStartTime;

    private LocalTime timeTableEndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @ParentId(Plan.class)
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
