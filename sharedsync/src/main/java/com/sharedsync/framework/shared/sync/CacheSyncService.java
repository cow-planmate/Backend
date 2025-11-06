package com.sharedsync.framework.shared.sync;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.shared.generated.cache.PlanCache;
import com.example.planmate.domain.shared.generated.cache.TimeTableCache;
import com.example.planmate.domain.shared.generated.cache.TimeTablePlaceBlockCache;
import com.example.planmate.domain.shared.generated.sharedDto.PlanDto;
import com.example.planmate.domain.shared.generated.sharedDto.TimeTableDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CacheSyncService {

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;


    public void syncToDatabase(int planId) {
        PlanDto planDto = planCache.findDtoById(planId);
        PlanDto updatedPlanDto = planCache.syncToDatabaseByDto(planDto);

        timeTableCache.syncToDatabaseByParentId(updatedPlanDto.planId());
        List<TimeTableDto> refreshedTimeTables = timeTableCache.findDtoListByParentId(planId);

        for (TimeTableDto timeTableDto : refreshedTimeTables) {
            Integer timeTableId = timeTableDto.timeTableId();
            timeTablePlaceBlockCache.syncToDatabaseByParentId(timeTableId);
            timeTablePlaceBlockCache.deleteCacheByParentId(timeTableId);
        }
        timeTableCache.deleteCacheByParentId(planId);
        planCache.deleteCacheById(planId);
    }
}