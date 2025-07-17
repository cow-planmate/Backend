package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.entity.TransportationCategory;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import com.example.planmate.repository.TransportationCategoryRepository;
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

    public void savePlan(int userId, int planId, String departure, int transportationCategoryId, int adultCount, int childCount, List<TimetableVO> timetables, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockLists) {
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        TransportationCategory transportationCategory = transportationCategoryRepository.findById(transportationCategoryId).get();
        plan.setDeparture(departure);
        plan.setTransportationCategory(transportationCategory);
        plan.setAdultCount(adultCount);
        plan.setChildCount(childCount);

        List<TimeTable> timeTables = changeTimetable(plan, timetables);
        changeTimetablePlaceBlock(plan, timetablePlaceBlockLists, timeTables);
    }

    private List<TimeTable> changeTimetable(Plan plan, List<TimetableVO> timetables) {
        timeTableRepository.deleteByPlan(plan);
        List<TimeTable> afterTimeTables = new ArrayList<>();
        for (TimetableVO timetable : timetables) {
                afterTimeTables.add(TimeTable.builder()
                        .date(timetable.getDate())
                        .timeTableStartTime(LocalTime.of(9,0))
                        .timeTableEndTime(LocalTime.of(20,0))
                        .plan(plan)
                        .build());
            timeTableRepository.saveAll(afterTimeTables);
        }
        return afterTimeTables;
    }
    private void changeTimetablePlaceBlock(Plan plan, List<List<TimetablePlaceBlockVO>> timetablePlaceBlockLists, List<TimeTable> timeTables) {
        timeTablePlaceBlockRepository.deleteAllByTimeTable_Plan(plan);
        List<TimeTablePlaceBlock> timeTablePlaceBlocks = new ArrayList<>();
        for(int i = 0; i < timetablePlaceBlockLists.size(); i++){
            TimeTable timetable = timeTables.get(i);
            for(int j = 0; j < timetablePlaceBlockLists.get(i).size(); j++){
                TimetablePlaceBlockVO timeTablePlaceBlockVO = timetablePlaceBlockLists.get(i).get(j);
                timeTablePlaceBlocks.add(TimeTablePlaceBlock.builder()
                        .timeTable(timetable)
                        .placeName(timeTablePlaceBlockVO.getPlaceName())
                        .placeTheme(timeTablePlaceBlockVO.getPlaceTheme())
                        .placeRating(timeTablePlaceBlockVO.getPlaceRating())
                        .placeAddress(timeTablePlaceBlockVO.getPlaceAddress())
                        .placeLink(timeTablePlaceBlockVO.getPlaceLink())
                        .blockStartTime(timeTablePlaceBlockVO.getStartTime())
                        .blockEndTime(timeTablePlaceBlockVO.getEndTime())
                        .xLocation(timeTablePlaceBlockVO.getXLocation())
                        .yLocation(timeTablePlaceBlockVO.getYLocation())
                        .build());
            }
        }
        timeTablePlaceBlockRepository.saveAll(timeTablePlaceBlocks);
    }
}
