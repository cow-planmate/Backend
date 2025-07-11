package com.example.planmate.service;

import com.example.planmate.dto.GetPreferredThemeResponse;
import com.example.planmate.dto.SavePreferredThemeResponse;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.repository.PreferredThemeRepository;
import com.example.planmate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PreferredThemeService {
    private final PreferredThemeRepository preferredThemeRepository;
    private final UserRepository userRepository;

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
        // 유저 조회
        userRepository.findById(userId).ifPresent(user -> {

            Set<PreferredTheme> themes = preferredThemeIds.stream()
                    .map(id -> preferredThemeRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마 ID: " + id)))
                    .collect(Collectors.toSet());

            user.setPreferredThemes(themes);
            userRepository.save(user);
        });
        return response;
    }
}
