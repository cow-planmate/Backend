package com.example.planmate.service;

import com.example.planmate.dto.MoveMypageResponse;
import com.example.planmate.entity.Plan;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.PlanEditorRepository;
import com.example.planmate.repository.PlanRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoveMypageService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PlanEditorRepository planEditorRepository;

    public MoveMypageResponse getMypageInfo(int userId) {
        MoveMypageResponse response = new MoveMypageResponse();

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        List<PreferredTheme> preferredThemes = new ArrayList<>(user.getPreferredThemes());
        for (PreferredTheme preferredTheme : preferredThemes) {
            response.addPreferredTheme(preferredTheme);
        }
        List<Plan> myPlans = planRepository.findByUserUserId(userId);
        for (Plan plan : myPlans) {
            response.addMyPlanVO(plan.getPlanId(), plan.getPlanName());
        }
        List<PlanEditor> editablePlanEditors = planEditorRepository.findByUserUserId(userId);
        for (PlanEditor editor : editablePlanEditors) {
            Plan plan = editor.getPlan();
            response.addEditablePlanVO(plan.getPlanId(), plan.getPlanName());
        }

        response.setMessage("Mypage info loaded successfully");
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setGender(user.getGender());

        return response;
    }
}
