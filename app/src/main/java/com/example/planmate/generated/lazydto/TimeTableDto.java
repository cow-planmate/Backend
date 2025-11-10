package com.example.planmate.generated.lazydto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.sharedsync.framework.shared.framework.annotation.AutoDatabaseLoader;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.ParentId;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @ParentId(Plan.class)
    private Integer planId;

    public static TimeTableDto fromEntity(TimeTable timeTable) {
        return instantiateFromEntity(timeTable, TimeTableDto.class);
    }

}