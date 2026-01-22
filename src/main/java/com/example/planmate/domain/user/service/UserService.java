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

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

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

        response.setMessage("성공적으로 마이페이지를 가져왔습니다");
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAge(user.getAge());
        response.setGender(user.getGender());
        response.setSocialLogin(user.isSocialLogin());

        return response;
    }
    @Transactional
    public ChangeNicknameResponse changeNickname(int userId, String nickname) {
        ChangeNicknameResponse response = new ChangeNicknameResponse();

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ResourceConflictException("이미 존재하는 닉네임입니다");
        }
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.changeNickname(nickname);

        response.setMessage("성공적으로 닉네임이 변경되었습니다");

        return response;
    }
    @Transactional
    public ChangeAgeResponse changeAge(int userId, int age) {
        ChangeAgeResponse response = new ChangeAgeResponse();

        if (age < 0) {
            throw new IllegalArgumentException("나이는 0 이상의 정수여야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.changeAge(age);

        response.setMessage("성공적으로 나이가 변경되었습니다");

        return response;
    }
    @Transactional
    public ChangeGenderResponse changeGender(int userId, int gender) {
        ChangeGenderResponse response = new ChangeGenderResponse();

        if (gender != 0 && gender != 1) {
            throw new IllegalArgumentException("gender 값은 0(남성) 또는 1(여성)이어야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        user.changeGender(gender);

        response.setMessage("성공적으로 성별이 변경되었습니다");

        return response;
    }

    @Transactional
    public ChangePreferredThemesResponse changePreferredThemes(int userId, int preferredThemeCategoryId, List<Integer> preferredThemeIds) {
        ChangePreferredThemesResponse response = new ChangePreferredThemesResponse();

        if (preferredThemeCategoryId != 0 && preferredThemeCategoryId != 1 && preferredThemeCategoryId != 2) {
            throw new IllegalArgumentException("preferredThemeCategoryId 값은 0(관광지) 또는 1(식당) 또는 2(숙소)이어야 합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<PreferredTheme> preferredTourThemes = user.getPreferredThemes();
        List<PreferredTheme> newThemes = preferredThemeRepository.findAllById(preferredThemeIds);

        preferredTourThemes.removeIf(theme ->
                theme.getPreferredThemeCategory() != null &&
                        theme.getPreferredThemeCategory().getPreferredThemeCategoryId() == preferredThemeCategoryId
        );

        user.getPreferredThemes().addAll(newThemes);

        response.setMessage("성공적으로 선호테마가 변경되었습니다");

        return response;
    }

    @Transactional
    public ResignAccountResponse resignAccount(int userId) {
        ResignAccountResponse response = new ResignAccountResponse();

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException();
        }

        userRepository.deleteById(userId);

        response.setMessage("성공적으로 계정이 삭제되었습니다");

        return response;
    }

    @Transactional
    public User findOrCreateOAuthUser(String provider, String providerId, String email, String nickname) {

        // 1) provider + providerId 기준으로 기존 유저 조회
        return userRepository.findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> {
                    // 2) 없으면 새로 생성 (SNS는 password 필요 없음)
                    User newUser = User.builder()
                            .provider(provider)
                            .providerId(providerId)
                            .email(email)
                            .nickname(nickname)
                            .password(null)   // SNS 로그인은 비번 없음
                            .age(null)
                            .gender(null)
                            .build();

                    return userRepository.save(newUser);
                });
    }

    public String resolveUniqueNickname(String baseNickname) {

        // 닉네임 중복 체크
        if (!userRepository.findByNickname(baseNickname).isPresent()) {
            return baseNickname; // 바로 사용 가능
        }

        // 중복이면 숫자 증가
        int index = 1;
        String newNickname;

        while (true) {
            newNickname = baseNickname + index;

            if (!userRepository.findByNickname(newNickname).isPresent()) {
                return newNickname;
            }

            index++;
        }
    }

    public String sanitizeNickname(String nickname) {

        if (nickname == null || nickname.isBlank()) {
            return "user";
        }

        String cleaned = nickname;

        // 1) 앞뒤 공백 제거
        cleaned = cleaned.trim();

        // 2) 모든 공백 제거 (중간 공백도)
        cleaned = cleaned.replaceAll("\\s+", "");

        // 3) 이모지 제거 (유니코드 범위 기준)
        cleaned = cleaned.replaceAll("[\\p{So}\\p{Cn}]", "");

        // 4) 특수문자 제거 (한글, 영문, 숫자만 허용)
        cleaned = cleaned.replaceAll("[^a-zA-Z0-9가-힣]", "");

        // 5) sanitize 후 값이 비면 fallback
        if (cleaned.isBlank()) {
            cleaned = "user";
        }

        // 6) 너무 길면 20자 제한 (선택 — UX상 좋음)
        if (cleaned.length() > 20) {
            cleaned = cleaned.substring(0, 20);
        }

        return cleaned;
    }

}
