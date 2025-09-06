package com.example.planmate.domain.user.service;

import com.example.planmate.common.exception.ResourceConflictException;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.user.dto.*;
import com.example.planmate.domain.user.entity.PreferredTheme;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.common.exception.UserNotFoundException;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.user.repository.PreferredThemeRepository;
import com.example.planmate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PreferredThemeRepository preferredThemeRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final PlanEditorRepository planEditorRepository;

    public GetPreferredThemeResponse getPreferredTheme() {
        GetPreferredThemeResponse response = new GetPreferredThemeResponse();
        List<PreferredTheme> preferredThemes= preferredThemeRepository.findAll();
        for (PreferredTheme preferredTheme : preferredThemes) {
            response.addPreferredTheme(preferredTheme);
        }
        return response;
    }

    public SavePreferredThemeResponse savePreferredTheme(int userId, List<Integer> preferredThemeIds) {
        SavePreferredThemeResponse response = new SavePreferredThemeResponse();

        userRepository.findById(userId).ifPresent(user -> {
            List<PreferredTheme> themes = preferredThemeIds.stream()
                    .map(id -> preferredThemeRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마 ID: " + id)))
                    .toList();

            user.getPreferredThemes().clear();
            user.getPreferredThemes().addAll(themes);

            userRepository.save(user);
        });
        return response;
    }

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
    @Transactional
    public ChangeNicknameResponse changeNickname(int userId, String nickname) {
        ChangeNicknameResponse response = new ChangeNicknameResponse();

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ResourceConflictException("이미 존재하는 닉네임입니다");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        user.changeNickname(nickname);

        response.setMessage("Nickname changed successfully");

        return response;
    }
    @Transactional
    public ChangeAgeResponse changeAge(int userId, int age) {
        ChangeAgeResponse response = new ChangeAgeResponse();

        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상의 정수여야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        user.changeAge(age);

        response.setMessage("Age changed successfully");

        return response;
    }
    @Transactional
    public ChangeGenderResponse changeGender(int userId, int gender) {
        ChangeGenderResponse response = new ChangeGenderResponse();

        if (gender != 0 && gender != 1) {
            throw new IllegalArgumentException("gender 값은 0(남성) 또는 1(여성)이어야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));

        user.changeGender(gender);

        response.setMessage("Gender changed successfully");

        return response;
    }

    @Transactional
    public ChangePreferredThemesResponse changePreferredThemes(int userId, int preferredThemeCategoryId, List<Integer> preferredThemeIds) {
        ChangePreferredThemesResponse response = new ChangePreferredThemesResponse();

        if (preferredThemeCategoryId != 0 && preferredThemeCategoryId != 1 && preferredThemeCategoryId != 2) {
            throw new IllegalArgumentException("preferredThemeCategoryId 값은 0(관광지) 또는 1(식당) 또는 2(숙소)이어야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("존재하지 않는 유저 ID입니다"));
        List<PreferredTheme> preferredTourThemes = user.getPreferredThemes();
        List<PreferredTheme> newThemes = preferredThemeRepository.findAllById(preferredThemeIds);

        preferredTourThemes.removeIf(theme ->
                theme.getPreferredThemeCategory() != null &&
                        theme.getPreferredThemeCategory().getPreferredThemeCategoryId() == preferredThemeCategoryId
        );

        user.getPreferredThemes().addAll(newThemes);

        response.setMessage("Preferred themes changed successfully");

        return response;
    }

    @Transactional
    public ResignAccountResponse resignAccount(int userId) {
        ResignAccountResponse response = new ResignAccountResponse();

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("존재하지 않는 유저 ID입니다");
        }

        userRepository.deleteById(userId);

        response.setMessage("The account has been successfully deleted.");

        return response;
    }
}
