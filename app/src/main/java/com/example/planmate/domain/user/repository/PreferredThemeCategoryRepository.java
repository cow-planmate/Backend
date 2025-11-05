package com.example.planmate.domain.user.repository;

import com.example.planmate.domain.user.entity.PreferredThemeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferredThemeCategoryRepository extends JpaRepository<PreferredThemeCategory, Integer> {
}
