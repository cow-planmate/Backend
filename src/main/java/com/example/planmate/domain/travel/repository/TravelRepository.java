package com.example.planmate.domain.travel.repository;

import com.example.planmate.domain.travel.entity.Travel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelRepository extends JpaRepository<Travel, Integer> {
}
