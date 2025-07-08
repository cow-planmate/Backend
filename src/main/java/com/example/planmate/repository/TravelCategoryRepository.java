package com.example.planmate.repository;

import com.example.planmate.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelCategoryRepository extends JpaRepository<TravelCategory, Integer> {
}
