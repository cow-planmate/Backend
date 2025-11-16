package com.example.planmate.generated.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.move.shared.framework.repository.AutoCacheRepository;
import com.example.planmate.generated.lazydto.TimeTableDto;

@Component
public class TimeTableCache extends AutoCacheRepository<TimeTable, Integer, TimeTableDto> {

}
