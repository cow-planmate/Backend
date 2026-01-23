package com.example.planmate.domain.place.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;

public interface PlaceSearchResultRepository extends JpaRepository<PlaceSearchResult, Long> {
    List<PlaceSearchResult> findAllByCondition(PlaceSearchCondition condition);
    Optional<PlaceSearchResult> findFirstByPlaceIdAndPhotoUrlIsNotNull(String placeId);
}
