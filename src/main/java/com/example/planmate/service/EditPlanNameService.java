package com.example.planmate.service;

import com.example.planmate.auth.PlanAccessValidator;
import com.example.planmate.dto.EditPlanNameResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditPlanNameService {
    private final PlanRepository planRepository;
    private final PlanAccessValidator planAccessValidator;

    public EditPlanNameResponse EditPlanName(int userId, int planId, String name){
        EditPlanNameResponse reponse = new EditPlanNameResponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        plan.setPlanName(name);
        planRepository.save(plan);
        return reponse;
    }
}
