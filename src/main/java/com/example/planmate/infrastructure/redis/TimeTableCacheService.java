package com.example.planmate.infrastructure.redis;

import static com.example.planmate.infrastructure.redis.RedisKeys.planToTimeTable;
import static com.example.planmate.infrastructure.redis.RedisKeys.timeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final AtomicInteger tempIdGenerator = new AtomicInteger(-1);

    public List<TimeTable> getByPlan(int planId){
        List<Integer> ids = planToTimeTableRedis.opsForValue().get(planToTimeTable(planId));
        if(ids==null || ids.isEmpty()) return Collections.emptyList();
        List<String> keys = new ArrayList<>(ids.size());
        for(Integer id : ids){ keys.add(timeTable(id)); }
        List<TimeTableDto> dtos = timeTableRedis.opsForValue().multiGet(keys);
        if(dtos==null) return Collections.emptyList();
        Plan planRef = planRepository.getReferenceById(planId);
        List<TimeTable> result = new ArrayList<>(dtos.size());
        for(TimeTableDto dto : dtos){ if(dto!=null) result.add(dto.toEntity(planRef)); }
        return result;
    }

    public void loadForPlan(int planId){
        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<Integer> ids = new ArrayList<>();
        for(TimeTable tt : timeTables){
            TimeTableDto dto = TimeTableDto.fromEntity(tt);
            timeTableRedis.opsForValue().set(timeTable(dto.timeTableId()), dto);
            ids.add(dto.timeTableId());
        }
        planToTimeTableRedis.opsForValue().set(planToTimeTable(planId), ids);
    }

    public int addNew(int planId, TimeTable timetable){
        int tempId = tempIdGenerator.getAndDecrement();
        timetable.changeId(tempId);
        timeTableRedis.opsForValue().set(timeTable(timetable.getTimeTableId()), TimeTableDto.fromEntity(timetable));
        List<Integer> ids = planToTimeTableRedis.opsForValue().get(planToTimeTable(planId));
        if(ids==null){ ids = new ArrayList<>(); }
        ids.add(timetable.getTimeTableId());
        planToTimeTableRedis.opsForValue().set(planToTimeTable(planId), ids);
        return tempId;
    }

    public TimeTable get(int id){
        TimeTableDto dto = timeTableRedis.opsForValue().get(timeTable(id));
        if(dto==null) throw new IllegalStateException("TimeTable cache miss: "+id);
        Plan planRef = planRepository.getReferenceById(dto.planId());
        return dto.toEntity(planRef);
    }

    public void update(TimeTable tt){
        timeTableRedis.opsForValue().set(timeTable(tt.getTimeTableId()), TimeTableDto.fromEntity(tt));
    }
}
