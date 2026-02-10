package com.example.planmate.domain.plan.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;

public interface TimeTableRepository extends JpaRepository<TimeTable, Integer> {
    List<TimeTable> findByPlanPlanId(UUID planId);

    void deleteByPlan(Plan plan);
}
