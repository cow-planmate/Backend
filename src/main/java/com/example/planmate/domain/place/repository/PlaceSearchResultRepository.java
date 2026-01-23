package com.example.planmate.domain.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;

public interface PlaceSearchResultRepository extends JpaRepository<PlaceSearchResult, Long> {
    List<PlaceSearchResult> findAllByCondition(PlaceSearchCondition condition);
    Optional<PlaceSearchResult> findFirstByPlaceIdAndPhotoUrlIsNotNull(String placeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PlaceSearchResult psr WHERE psr.condition = :condition")
    void deleteAllByCondition(@Param("condition") PlaceSearchCondition condition);

    @Modifying
    @Transactional
    @Query("UPDATE PlaceSearchResult psr SET psr.photoUrl = :photoUrl WHERE psr.placeId = :placeId AND (psr.photoUrl IS NULL OR psr.photoUrl = '')")
    void updatePhotoUrlByPlaceId(@Param("placeId") String placeId, @Param("photoUrl") String photoUrl);
}
