package com.example.planmate.service;

import com.example.planmate.dto.ChangePreferredThemesResponse;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.entity.User;
import com.example.planmate.exception.UserNotFoundException;
import com.example.planmate.repository.PreferredThemeRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangePreferredThemesService {

    private final UserRepository userRepository;
    private final PreferredThemeRepository preferredThemeRepository;

    @Transactional
    public ChangePreferredThemesResponse changePreferredThemes(int userId, int preferredThemeCategoryId, List<Integer> preferredThemeIds) {
        ChangePreferredThemesResponse response = new ChangePreferredThemesResponse();

        if (preferredThemeCategoryId != 0 && preferredThemeCategoryId != 1) {
            throw new IllegalArgumentException("preferredThemeCategoryId 값은 0(관광지) 또는 1(식당)이어야 합니다.");
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
}
