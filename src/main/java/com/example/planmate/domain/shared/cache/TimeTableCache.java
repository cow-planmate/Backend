package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.lazydto.TimeTableDto;

@Component
public class TimeTableCache extends SuperAutoCacheRepository<TimeTable, Integer, TimeTableDto> {
}
