package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanRepository extends JpaRepository<Plan, UUID> {
    List<Plan> findByUserUserId(UUID userId);
    boolean existsByUserUserIdAndPlanName(UUID userId, String planName);
    boolean existsByPlanIdAndUserUserId(UUID planId, UUID userId);

    boolean existsByUser_UserIdAndPlanName(UUID userId, String name);

    List<Plan> findAllByPlanIdInAndUserUserId(List<UUID> planIds, UUID userId);
}
