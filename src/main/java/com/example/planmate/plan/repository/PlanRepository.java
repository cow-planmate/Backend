package com.example.planmate.plan.repository;

import com.example.planmate.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByUserUserId(Integer userId);
    boolean existsByUserUserIdAndPlanName(Integer userId, String planName);
    boolean existsByPlanIdAndUserUserId(Integer planId, Integer userId);
}
