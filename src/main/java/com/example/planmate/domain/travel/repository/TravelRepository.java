package com.example.planmate.domain.travel.repository;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TravelRepository extends JpaRepository<Travel, Integer> {
    Optional<Travel> findByTravelName(String travelName);

    Optional<Travel> findByTravelNameAndTravelCategory(
            String travelName,
            TravelCategory travelCategory
    );

    @Query("""
    SELECT t
    FROM Travel t
    JOIN FETCH t.travelCategory
    """)
    List<Travel> findAllWithCategory();
}
