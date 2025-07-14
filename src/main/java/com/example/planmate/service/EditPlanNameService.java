package com.example.planmate.service;

import com.example.planmate.Auth.PlanAccessValidator;
import com.example.planmate.dto.EditPlanNameReponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditPlanNameService {
    private PlanRepository planRepository;
    private PlanAccessValidator planAccessValidator;

    public EditPlanNameReponse EditPlanName(int userId, int planId, String name){
        EditPlanNameReponse reponse = new EditPlanNameReponse();
        Plan plan = planAccessValidator.validateUserHasAccessToPlan(userId, planId);
        plan.setPlanName(name);
        planRepository.save(plan);
        return reponse;
    }
}
