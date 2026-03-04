package com.example.planmate.domain.travel.repository;

import com.example.planmate.domain.travel.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelCategoryRepository extends JpaRepository<TravelCategory, Integer> {
    Optional<TravelCategory> findByTravelCategoryName(String categoryName);
}
