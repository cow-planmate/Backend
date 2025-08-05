package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.DeletePlanResponse;
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
public class DeletePlanService {
    private final PlanRepository planRepository;

    public DeletePlanResponse deletePlan(int userId, int planId) {
        DeletePlanResponse response = new DeletePlanResponse();

        if (!planRepository.existsById(planId)) {
            response.setMessage("해당 플랜이 존재하지 않습니다.");
            return response;
        }

        boolean isOwner = planRepository.existsByPlanIdAndUserUserId(planId, userId);
        if (!isOwner) {
            response.setMessage("일정을 삭제할 권한이 없습니다.");
        } else {
            planRepository.deleteById(planId);
            response.setMessage("일정을 삭제했습니다.");
        }

        return response;
    }
}
