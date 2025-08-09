package com.example.planmate.domain.user.repository;

import com.example.planmate.domain.user.entity.PreferredTheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferredThemeRepository extends JpaRepository<PreferredTheme, Integer> {

    public PreferredTheme findByPreferredThemeName(String preferredThemeName);
    public List<PreferredTheme> findPreferredThemesByPreferredThemeCategory_PreferredThemeCategoryId(int preferredThemeCategoryId);
}
