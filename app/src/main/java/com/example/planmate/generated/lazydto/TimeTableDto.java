package com.example.planmate.generated.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.move.shared.framework.annotation.AutoDatabaseLoader;
import com.example.planmate.move.shared.framework.annotation.AutoEntityConverter;
import com.example.planmate.move.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.move.shared.framework.annotation.CacheEntity;
import com.example.planmate.move.shared.framework.annotation.CacheId;
import com.example.planmate.move.shared.framework.annotation.EntityConverter;
import com.example.planmate.move.shared.framework.annotation.ParentId;
import com.example.planmate.move.shared.framework.dto.CacheDto;

import com.example.planmate.move.shared.presence.annotation.PresenceKey;
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
    @PresenceKey(
            name = "date",      // JSON key
            level = 0,            // 계층 최상위
            identifier = true     // Presence 식별자
    )
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