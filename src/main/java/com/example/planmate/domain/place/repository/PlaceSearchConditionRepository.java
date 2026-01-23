package com.example.planmate.domain.place.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.place.entity.PlaceSearchCondition;

public interface PlaceSearchConditionRepository extends JpaRepository<PlaceSearchCondition, Long> {
    Optional<PlaceSearchCondition> findByCacheKey(String cacheKey);
}
