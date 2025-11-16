package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.sharedsync.framework.shared.framework.annotation.*;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@CacheEntity
@AutoRedisTemplate("timeTableRedis")
@AutoDatabaseLoader(repository = "timeTableRepository", method = "findByPlanPlanId")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeTableDto extends EntityBackedCacheDto<Integer, TimeTable> {

    @CacheId
    private Integer timeTableId;
    private LocalDate date;
    private LocalTime timeTableStartTime;
    private LocalTime timeTableEndTime;
    @EntityReference(repository = "planRepository", entityType = Plan.class, optional = false)
    @ParentId(Plan.class)
    private Integer planId;

    public static TimeTableDto fromEntity(TimeTable timeTable) {
        return instantiateFromEntity(timeTable, TimeTableDto.class);
    }

}