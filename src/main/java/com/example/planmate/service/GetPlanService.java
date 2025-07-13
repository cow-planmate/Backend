package com.example.planmate.service;

import com.example.planmate.dto.GetPlanResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.TimeTable;
import com.example.planmate.entity.TimeTablePlaceBlock;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPlanService {
    private final PlanRepository planRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    public GetPlanResponse getPlan(int userId, int planId) throws AccessDeniedException {
        GetPlanResponse response = new GetPlanResponse();
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("플랜 없음"));

        if (plan.getUser().getUserId() != userId) {
            throw new AccessDeniedException("권한 없음");
        }
        response.addPlanFrame(
                planId,
                plan.getPlanName(),
                plan.getDeparture(),
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
                        timeTablePlaceBlock1.getPlaceName(),
                        timeTablePlaceBlock1.getPlaceTheme(),
                        timeTablePlaceBlock1.getPlaceRating(),
                        timeTablePlaceBlock1.getPlaceAddress(),
                        timeTablePlaceBlock1.getPlaceLink(),
                        timeTablePlaceBlock1.getXLocation(),
                        timeTablePlaceBlock1.getYLocation()
                );
            }
        }

        return response; // DTO 변환
    }
}
