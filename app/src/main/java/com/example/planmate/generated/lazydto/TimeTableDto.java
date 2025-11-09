package com.example.planmate.generated.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.sharedsync.framework.shared.framework.annotation.AutoDatabaseLoader;
import com.sharedsync.framework.shared.framework.annotation.AutoEntityConverter;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.sharedsync.framework.shared.framework.annotation.ParentId;
import com.sharedsync.framework.shared.framework.dto.CacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@AutoRedisTemplate("timeTableRedis")
@AutoDatabaseLoader(repository = "timeTableRepository", method = "findByPlanPlanId")
@AutoEntityConverter(repositories = {"planRepository"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeTableDto extends CacheDto<Integer> {

    @CacheId
    private Integer timeTableId;
    private LocalDate date;
    private LocalTime timeTableStartTime;
    private LocalTime timeTableEndTime;
    @ParentId(Plan.class)
    private Integer planId;

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

}