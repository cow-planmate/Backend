package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.shared.framework.repository.AutoCacheRepository;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

@Component
public class TimeTablePlaceBlockCache extends AutoCacheRepository<TimeTablePlaceBlock, Integer, TimeTablePlaceBlockDto> {
}
