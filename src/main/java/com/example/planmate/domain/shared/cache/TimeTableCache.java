package com.example.planmate.domain.shared.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.shared.enums.ECasheKey;
import com.example.planmate.domain.shared.lazydto.TimeTableDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeTableCache {

    private final RedisTemplate<String, TimeTableDto> timeTableRedis;
    private final RedisTemplate<String, Integer> planToTimeTableRedis;
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);
    private final RedisTemplate<String, Integer> timeTableToTimeTablePlaceBlockRedis;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;
    
    public TimeTable findTimeTableByTimeTableId(int timetableId) {
        TimeTableDto cached = timeTableRedis.opsForValue().get(ECasheKey.TIMETABLE.key(timetableId));

        if (cached == null) {
            throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다: "+ timetableId);
        }
        Plan planRef = planRepository.getReferenceById(cached.planId());
        return cached.toEntity(planRef);
    }

    public List<TimeTable> findTimeTablesByPlanId(int planId) {
        Set<Integer> timeTableIds = planToTimeTableRedis.opsForSet().members(ECasheKey.PLANTOTIMETABLE.key(planId));
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(ECasheKey.TIMETABLE.key(timeTableId));
        }
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) {
            if (dto != null) result.add(dto.toEntity(planRef));
        }
        return result;
    }

    public List<TimeTable> deleteTimeTableByPlanId(int planId) {
        Set<Integer> timeTableIds = planToTimeTableRedis.opsForSet().members(ECasheKey.PLANTOTIMETABLE.key(planId));
        planToTimeTableRedis.delete(ECasheKey.PLANTOTIMETABLE.key(planId));
        if (timeTableIds == null || timeTableIds.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(timeTableIds.size());
        for(Integer timeTableId : timeTableIds){
            keys.add(ECasheKey.TIMETABLE.key(timeTableId));
        }
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) {
            if (dto != null) result.add(dto.toEntity(planRef));
        }
        return result;
    }

    public List<TimeTableDto> insertTimeTablesByPlanId(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<TimeTableDto> result = new ArrayList<>();
        for(TimeTable timeTable : timeTables){
            TimeTableDto dto = TimeTableDto.fromEntity(timeTable);
            timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(dto.timeTableId()), dto);
            planToTimeTableRedis.opsForSet().add(ECasheKey.PLANTOTIMETABLE.key(planId), dto.timeTableId());
            result.add(dto);
        }
        return result;
    }

    public int createTimeTable(int planId, TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndDecrement();
        timetable.changeId(tempId);
        timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(timetable.getTimeTableId()), TimeTableDto.fromEntity(timetable));
        planToTimeTableRedis.opsForSet().add(ECasheKey.PLANTOTIMETABLE.key(planId), timetable.getTimeTableId());
        return tempId;
    }

    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) {
        for(TimetableVO timeTable : timeTableVOs){
            if(timeTable.getTimetableId() != null){
                timeTableRedis.delete(ECasheKey.TIMETABLE.key(timeTable.getTimetableId()));
                planToTimeTableRedis.opsForSet().remove(ECasheKey.PLANTOTIMETABLE.key(planId), timeTable.getTimetableId());
                Set<Integer> timeTablePlaceBlocks = timeTableToTimeTablePlaceBlockRedis.opsForSet()
                        .members(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTable.getTimetableId()));
                if(timeTablePlaceBlocks != null && !timeTablePlaceBlocks.isEmpty()) {
                    for(int timeTablePlaceBlockId : timeTablePlaceBlocks){
                        timeTablePlaceBlockCache.deleteTimeTablePlaceBlockById(timeTablePlaceBlockId);
                    }
                    timeTableToTimeTablePlaceBlockRedis.delete(ECasheKey.TIMETABLETOTIMETABLEPLACEBLOCK.key(timeTable.getTimetableId()));
                }
            }
        }
    }

    public void deleteRedisTimeTable(List<Integer> timetableIds) {
        List<String> timetableKeys = timetableIds.stream()
            .map(id -> ECasheKey.TIMETABLE.key(id))
            .toList();
        timeTableRedis.delete(timetableKeys);
        timeTablePlaceBlockCache.deleteRedisTimeTableBlockByTimeTableId(timetableIds);
    }


    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(ECasheKey.TIMETABLE.key(timeTable.getTimeTableId()), TimeTableDto.fromEntity(timeTable));
    }

}
