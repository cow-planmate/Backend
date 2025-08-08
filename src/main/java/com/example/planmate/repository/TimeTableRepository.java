package com.example.planmate.repository;

import com.example.planmate.plan.entity.Plan;
import com.example.planmate.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeTableRepository extends JpaRepository<TimeTable, Integer> {
    List<TimeTable> findByPlanPlanId(Integer planId);

    void deleteByPlan(Plan plan);
}
