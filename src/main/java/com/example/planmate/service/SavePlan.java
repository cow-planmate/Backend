package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TransportationCategory;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import com.example.planmate.repository.TransportationCategoryRepository;
import com.example.planmate.valueObject.PlanFrameVO;
import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.valueObject.TimetableVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavePlan {
    private final PlanRepository planRepository;
    private final TransportationCategoryRepository transportationCategoryRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    private final PlanAccessValidator planAccessValidator;

    public void savePlan(int userId, int planId, String departure, int transportationCategoryId, int adultCount, int childCount, List<TimetableVO> timetables, List<TimetablePlaceBlockVO> timetablePlaceBlocks) {
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        TransportationCategory transportationCategory = transportationCategoryRepository.findById(transportationCategoryId).get();
        plan.setDeparture(departure);
        plan.setTransportationCategory(transportationCategory);
        plan.setAdultCount(adultCount);
        plan.setChildCount(childCount);

        changeTimetable(planId, timetables);
        changeTimetablePlaceBlock(planId, timetablePlaceBlocks);

    }

    private void changeTimetable(int planId, List<TimetableVO> timetables) {
        for (TimetableVO timetable : timetables) {
            List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
            List<TimeTable> afterTimeTables = new ArrayList<>();

            if(timetable.getTimetableId()==0){
                afterTimeTables.add(TimeTable.builder()
                        .date(timetable.getDate())
                        .timeTableStartTime(LocalTime.of(9,0))
                        .timeTableEndTime(LocalTime.of(20,0))
                        .build());
            }
            else{
                TimeTable timeTable = timeTables.get(timetable.getTimetableId());
                timeTable.setTimeTableStartTime(timeTable.getTimeTableStartTime());
                timeTable.setTimeTableEndTime(timeTable.getTimeTableEndTime());
                timeTable.setDate(timetable.getDate());
                afterTimeTables.add(timeTable);
            }
            timeTableRepository.saveAll(afterTimeTables);
        }
    }
    private void changeTimetablePlaceBlock(int planId, List<TimetablePlaceBlockVO> timetablePlaceBlocks) {
        
    }
}
