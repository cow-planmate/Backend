package com.example.planmate.domain.redis.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.webSocket.lazydto.TimeTableDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeTableCacheService {
    private final RedisTemplate<String, TimeTableDto> timeTableRedis;
    private final RedisTemplate<String, List<Integer>> planToTimeTableRedis;
    private static final String TIMETABLE_PREFIX = "TIMETABLE";
    private static final String PLANTOTIMETABLE_PREFIX = "PLANTOTIMETABLE";

    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final AtomicInteger timeTableTempIdGenerator = new AtomicInteger(-1);

    private final TimeTablePlaceBlockCacheService placeBlockCacheService;

    public TimeTable getTimeTable(int timetableId) {
        TimeTableDto cached = timeTableRedis.opsForValue().get(TIMETABLE_PREFIX + timetableId);
        if (cached == null) throw new IllegalStateException("캐시 누락: Redis에 저장된 TimeTable 정보가 없습니다.");
        Plan planRef = planRepository.getReferenceById(cached.planId());
        return cached.toEntity(planRef);
    }

    public List<TimeTable> getTimeTableByPlanId(int planId) {
        List<Integer> ids = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(ids.size());
        for (Integer id : ids) keys.add(TIMETABLE_PREFIX + id);
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) if (dto != null) result.add(dto.toEntity(planRef));
        return result;
    }

    public List<TimeTable> deleteTimeTableByPlanId(int planId) {
        List<Integer> ids = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        planToTimeTableRedis.delete(PLANTOTIMETABLE_PREFIX + planId);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(ids.size());
        for (Integer id : ids) keys.add(TIMETABLE_PREFIX + id);
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if (dtos == null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for (TimeTableDto dto : dtos) if (dto != null) result.add(dto.toEntity(planRef));
        // cleanup place blocks
        placeBlockCacheService.deleteByTimeTableIds(ids);
        return result;
    }

    public List<TimeTableDto> registerTimeTable(int planId) {
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<Integer> ids = new ArrayList<>();
        List<TimeTableDto> result = new ArrayList<>();
        for (TimeTable timeTable : timeTables) {
            TimeTableDto dto = TimeTableDto.fromEntity(timeTable);
            timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + dto.timeTableId(), dto);
            ids.add(dto.timeTableId());
            result.add(dto);
        }
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, ids);
        return result;
    }

    public int registerNewTimeTable(int planId, TimeTable timetable) {
        int tempId = timeTableTempIdGenerator.getAndDecrement();
        timetable.changeId(tempId);
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timetable.getTimeTableId(), TimeTableDto.fromEntity(timetable));
        List<Integer> ids = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
        if (ids == null) ids = new ArrayList<>();
        ids.add(timetable.getTimeTableId());
        planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, ids);
        return tempId;
    }

    public void deleteRedisTimeTable(List<Integer> timetableIds) {
        if (timetableIds == null || timetableIds.isEmpty()) return;
        List<String> keys = timetableIds.stream().filter(Objects::nonNull).map(id -> TIMETABLE_PREFIX + id).toList();
        timeTableRedis.delete(keys);
        placeBlockCacheService.deleteByTimeTableIds(timetableIds);
    }

    public void deleteTimeTable(int planId, List<TimetableVO> timeTableVOs) {
        for (TimetableVO tt : timeTableVOs) {
            if (tt.getTimetableId() != null) {
                timeTableRedis.delete(TIMETABLE_PREFIX + tt.getTimetableId());
                List<Integer> ids = planToTimeTableRedis.opsForValue().get(PLANTOTIMETABLE_PREFIX + planId);
                if (ids != null) {
                    ids.remove(tt.getTimetableId());
                    planToTimeTableRedis.opsForValue().set(PLANTOTIMETABLE_PREFIX + planId, ids);
                }
                placeBlockCacheService.deleteByTimeTableId(tt.getTimetableId());
            }
        }
    }

    public void updateTimeTable(TimeTable timeTable) {
        timeTableRedis.opsForValue().set(TIMETABLE_PREFIX + timeTable.getTimeTableId(), TimeTableDto.fromEntity(timeTable));
    }
}
