package com.example.planmate.domain.shared.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.framework.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.framework.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.framework.annotation.CacheEntity;
import com.example.planmate.domain.shared.framework.annotation.CacheId;
import com.example.planmate.domain.shared.framework.annotation.EntityConverter;
import com.example.planmate.domain.shared.framework.annotation.ParentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@CacheEntity
@AutoRedisTemplate("timeTableRedis")
@AutoDatabaseLoader(repository = "timeTableRepository", method = "findByPlanPlanId")
@AutoEntityConverter(repositories = {"planRepository"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class TimeTableDto {

    @CacheId
    private Integer timeTableId;
    private LocalDate date;
    private LocalTime timeTableStartTime;
    private LocalTime timeTableEndTime;
    @ParentId
    private Integer planId;

    public static TimeTableDto fromEntity(TimeTable timeTable) {
        return TimeTableDto.builder()
                .timeTableId(timeTable.getTimeTableId())
                .date(timeTable.getDate())
                .timeTableStartTime(timeTable.getTimeTableStartTime())
                .timeTableEndTime(timeTable.getTimeTableEndTime())
                .planId(timeTable.getPlan().getPlanId())
                .build();
    }

    @EntityConverter
    public TimeTable toEntity(Plan plan) {
        return TimeTable.builder()
                .timeTableId(this.timeTableId)
                .date(this.date)
                .timeTableStartTime(this.timeTableStartTime)
                .timeTableEndTime(this.timeTableEndTime)
                .plan(plan)
                .build();
    }

    public TimeTableDto withTimeTableId(Integer newTimeTableId) {
        return this.toBuilder()
                .timeTableId(newTimeTableId)
                .build();
    }
}