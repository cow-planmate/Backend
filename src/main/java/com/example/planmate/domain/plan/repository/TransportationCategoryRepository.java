package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.TransportationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportationCategoryRepository extends JpaRepository<TransportationCategory, Integer> {
}
