package com.example.planmate.domain.travel.repository;

import com.example.planmate.domain.travel.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelCategoryRepository extends JpaRepository<TravelCategory, Integer> {
}
