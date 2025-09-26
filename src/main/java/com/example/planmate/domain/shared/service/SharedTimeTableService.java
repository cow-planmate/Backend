package com.example.planmate.domain.shared.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetableVO;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SharedTimeTableService implements SharedService<WTimetableRequest, WTimetableResponse> {

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;

    public WTimetableResponse create(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();

        Plan plan = planCache.findPlanByPlanId(request.getPlanId());
        for(TimetableVO timetableVO : timetableVOs) {
            TimeTable timeTable = TimeTable.builder()
                    .plan(plan)
                    .date(timetableVO.getDate())
                    .timeTableStartTime(timetableVO.getStartTime())
                    .timeTableEndTime(timetableVO.getEndTime())
                    .build();
            int tempId = timeTableCache.createTimeTable(request.getPlanId(), timeTable);
            timetableVO.setTimetableId(tempId);
            response.addTimetableVO(timetableVO);
        }

        return response;
    }

    public WTimetableResponse update(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimetableVO> timetableVOs = request.getTimetableVOs();
        for(TimetableVO timetableVO : timetableVOs) {
            int timetableId = timetableVO.getTimetableId();
            TimeTable timetable = timeTableCache.findTimeTableByTimeTableId(timetableId);
            if(timetable.getPlan().getPlanId() != request.getPlanId()) {
                throw new AccessDeniedException("timetable 접근 권한이 없습니다");
            }
            timetable.changeDate(timetableVO.getDate());
            timetable.changeTime(timetableVO.getStartTime(), timetableVO.getEndTime());
            timeTableCache.updateTimeTable(timetable);
            response.addTimetableVO(timetableVO);
        }
        response.sortTimetableVOs();
        return response;
    }

    public WTimetableResponse delete(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        timeTableCache.deleteTimeTable(request.getPlanId(), request.getTimetableVOs());
        response.setTimetableVOs(request.getTimetableVOs());
        return response;
    }


    
}
