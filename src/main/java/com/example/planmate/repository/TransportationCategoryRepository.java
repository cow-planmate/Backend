package com.example.planmate.repository;

import com.example.planmate.entity.TransportationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportationCategoryRepository extends JpaRepository<TransportationCategory, Integer> {
}
