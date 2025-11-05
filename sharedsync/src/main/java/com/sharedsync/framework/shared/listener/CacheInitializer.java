package com.sharedsync.framework.shared.listener;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sharedsync.framework.shared.cache.PlanCache;
import com.sharedsync.framework.shared.cache.TimeTableCache;
import com.sharedsync.framework.shared.cache.TimeTablePlaceBlockCache;
import com.sharedsync.framework.shared.lazydto.PlanDto;
import com.sharedsync.framework.shared.lazydto.TimeTableDto;
import com.sharedsync.framework.shared.lazydto.TimeTablePlaceBlockDto;

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
