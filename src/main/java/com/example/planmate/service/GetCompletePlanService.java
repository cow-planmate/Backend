package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.GetCompletePlanResponse;
import com.example.planmate.dto.GetPlanResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetCompletePlanService {
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;

    public GetCompletePlanResponse getCompletePlan(int planId) {
        GetCompletePlanResponse response = new GetCompletePlanResponse();
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));
        response.addPlanFrame(
                planId,
                plan.getPlanName(),
                plan.getDeparture(),
                plan.getTravel().getTravelId(),
                plan.getTravel().getTravelName(),
                plan.getAdultCount(),
                plan.getChildCount(),
                plan.getTransportationCategory().getTransportationCategoryId());

        List<TimeTable> timeTables = timeTableRepository.findByPlanPlanId(planId);
        List<List<TimeTablePlaceBlock>> timeTablePlaceBlocks = new ArrayList<>();

        for (TimeTable timeTable : timeTables) {
            timeTablePlaceBlocks.add(timeTablePlaceBlockRepository.findByTimeTableTimeTableId(timeTable.getTimeTableId()));
        }

        for (TimeTable timeTable : timeTables){
            response.addTimetable(timeTable.getTimeTableId(), timeTable.getDate(), timeTable.getTimeTableStartTime(), timeTable.getTimeTableEndTime());
        }

        for (List<TimeTablePlaceBlock> timeTablePlaceBlock : timeTablePlaceBlocks) {
            for (TimeTablePlaceBlock timeTablePlaceBlock1 : timeTablePlaceBlock) {
                response.addPlaceBlock(timeTablePlaceBlock1.getBlockId(),
                        timeTablePlaceBlock1.getPlaceCategory().getPlaceCategoryId(),
                        timeTablePlaceBlock1.getPlaceName(),
                        timeTablePlaceBlock1.getPlaceTheme(),
                        timeTablePlaceBlock1.getPlaceRating(),
                        timeTablePlaceBlock1.getPlaceAddress(),
                        timeTablePlaceBlock1.getPlaceLink(),
                        timeTablePlaceBlock1.getXLocation(),
                        timeTablePlaceBlock1.getYLocation(),
                        timeTablePlaceBlock1.getBlockStartTime(),
                        timeTablePlaceBlock1.getBlockEndTime()
                );
            }
        }
        response.setMessage("성공적으로 일정 완성본을 전송하였습니다.");
        return response;
    }
}
