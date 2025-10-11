package com.example.planmate.domain.shared.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.cache.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.cache.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.cache.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.cache.annotation.CacheEntity;
import com.example.planmate.domain.shared.cache.annotation.CacheId;
import com.example.planmate.domain.shared.cache.annotation.EntityConverter;
import com.example.planmate.domain.shared.cache.annotation.ParentId;
import com.example.planmate.domain.shared.enums.ECasheKey;

@CacheEntity(keyType = ECasheKey.TIMETABLE)
@AutoRedisTemplate("timeTableRedis")
@AutoDatabaseLoader(repository = "timeTableRepository", method = "findByPlanPlanId")
@AutoEntityConverter(repositories = {"planRepository"})
public record TimeTableDto(
        @CacheId
        Integer timeTableId,
        LocalDate date,
        LocalTime timeTableStartTime,
        LocalTime timeTableEndTime,
        @ParentId
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