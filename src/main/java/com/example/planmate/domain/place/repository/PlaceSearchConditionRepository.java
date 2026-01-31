package com.example.planmate.domain.place.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.place.entity.PlaceSearchCondition;

public interface PlaceSearchConditionRepository extends JpaRepository<PlaceSearchCondition, Long> {
    Optional<PlaceSearchCondition> findByCacheKey(String cacheKey);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO place_search_condition (travel_id, place_category_id, preferred_theme_id, cache_key, expired_at) " +
                   "VALUES (:travelId, :placeCategoryId, :preferredThemeId, :cacheKey, :expiredAt) " +
                   "ON CONFLICT (cache_key) DO UPDATE " +
                   "SET expired_at = :expiredAt, travel_id = :travelId", nativeQuery = true)
    void upsertCondition(@Param("travelId") Integer travelId,
                         @Param("placeCategoryId") Integer placeCategoryId,
                         @Param("preferredThemeId") Integer preferredThemeId,
                         @Param("cacheKey") String cacheKey,
                         @Param("expiredAt") LocalDateTime expiredAt);
}
