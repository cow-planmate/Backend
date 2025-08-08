package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TimeTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimeTableRepository extends JpaRepository<TimeTable, Integer> {
    List<TimeTable> findByPlanPlanId(Integer planId);

    void deleteByPlan(Plan plan);
}
