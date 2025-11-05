package com.example.planmate.generated.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.sharedsync.framework.shared.framework.repository.AutoCacheRepository;
import com.example.planmate.generated.lazydto.TimeTablePlaceBlockDto;

@Component
public class TimeTablePlaceBlockCache extends AutoCacheRepository<TimeTablePlaceBlock, Integer, TimeTablePlaceBlockDto> {
}
