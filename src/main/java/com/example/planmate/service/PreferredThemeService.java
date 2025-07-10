package com.example.planmate.service;

import com.example.planmate.dto.GetPreferredThemeResponse;
import com.example.planmate.entity.PreferredTheme;
import com.example.planmate.repository.PreferredThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreferredThemeService {
    private final PreferredThemeRepository preferredThemeRepository;

    public GetPreferredThemeResponse getPreferredTheme() {
        List<PreferredTheme> preferredThemes= preferredThemeRepository.findAll();


    }
}
