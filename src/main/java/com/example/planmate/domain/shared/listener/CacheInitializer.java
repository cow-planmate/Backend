package com.example.planmate.domain.shared.listener;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;
import com.example.planmate.domain.shared.lazydto.PlanDto;
import com.example.planmate.domain.shared.lazydto.TimeTableDto;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;

    public void initializeHierarchy(int planId) {
        PlanDto planDto = planCache.loadFromDatabaseById(planId);
        if (planDto == null) {
            return;
        }
        planCache.save(planDto);

        List<TimeTableDto> timeTables = timeTableCache.loadFromDatabaseByParentId(planId);
        for (TimeTableDto timeTable : timeTables) {
            timeTableCache.save(timeTable);
            List<TimeTablePlaceBlockDto> blocks = timeTablePlaceBlockCache
                    .loadFromDatabaseByParentId(timeTable.timeTableId());
            blocks.forEach(timeTablePlaceBlockCache::save);
        }
    }
}
