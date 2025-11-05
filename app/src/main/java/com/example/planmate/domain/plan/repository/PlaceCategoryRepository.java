package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.PlaceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCategoryRepository extends JpaRepository<PlaceCategory, Integer> {
}