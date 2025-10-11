package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

@Component
public class TimeTablePlaceBlockCache extends SuperAutoCacheRepository<TimeTablePlaceBlock, Integer, TimeTablePlaceBlockDto> {
}
