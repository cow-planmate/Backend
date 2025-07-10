package com.example.planmate.repository;

import com.example.planmate.entity.PreferredTheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferredThemeRepository extends JpaRepository<PreferredTheme, Integer> {

}
