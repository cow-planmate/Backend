package com.example.planmate.domain.shared.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.dto.WPresencesRequest;
import com.example.planmate.domain.shared.dto.WPresencesResponse;
import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SharedTimeTableService {

    private final PlanCache redisService;

    public WTimetableResponse createTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();

        Plan plan = redisService.findPlanByPlanId(planId);
        for(TimetableVO timetableVO : timetableVOs) {
            TimeTable timeTable = TimeTable.builder()
                    .plan(plan)
                    .date(timetableVO.getDate())
                    .timeTableStartTime(timetableVO.getStartTime())
                    .timeTableEndTime(timetableVO.getEndTime())
                    .build();
            int tempId = redisService.createTimeTable(planId, timeTable);
            timetableVO.setTimetableId(tempId);
            response.addTimetableVO(timetableVO);
        }

        return response;
    }

    public WTimetableResponse updateTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();
        for(TimetableVO timetableVO : timetableVOs) {
            int timetableId = timetableVO.getTimetableId();
            TimeTable timetable = redisService.findTimeTableByTimeTableId(timetableId);
            if(timetable.getPlan().getPlanId() != planId) {
                throw new AccessDeniedException("timetable 접근 권한이 없습니다");
            }
            timetable.changeDate(timetableVO.getDate());
            timetable.changeTime(timetableVO.getStartTime(), timetableVO.getEndTime());
            redisService.updateTimeTable(timetable);
            response.addTimetableVO(timetableVO);
        }
        response.sortTimetableVOs();
        return response;
    }

    public WTimetableResponse deleteTimetable(int planId, WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        redisService.deleteTimeTable(planId, request.getTimetableVOs());
        response.setTimetableVOs(request.getTimetableVOs());
        return response;
    }


    
}
