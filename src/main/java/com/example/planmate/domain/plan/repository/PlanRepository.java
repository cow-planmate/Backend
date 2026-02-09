package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, String> {
    List<Plan> findByUserUserId(String userId);
    boolean existsByUserUserIdAndPlanName(String userId, String planName);
    boolean existsByPlanIdAndUserUserId(String planId, String userId);

    boolean existsByUser_UserIdAndPlanName(String userId, String name);

    List<Plan> findAllByPlanIdInAndUserUserId(List<String> planIds, String userId);
}
