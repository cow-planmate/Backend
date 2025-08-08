package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    List<Plan> findByUserUserId(Integer userId);
    boolean existsByUserUserIdAndPlanName(Integer userId, String planName);
    boolean existsByPlanIdAndUserUserId(Integer planId, Integer userId);
}
