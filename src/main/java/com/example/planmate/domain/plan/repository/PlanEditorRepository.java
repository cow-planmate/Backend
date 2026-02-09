package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanEditorRepository extends JpaRepository<PlanEditor, Integer> {

    boolean existsByUserAndPlan(User user, Plan plan);

    List<PlanEditor> findByUserUserId(String userId);

    Optional<PlanEditor> findByUser_UserIdAndPlan_PlanId(String userId, String planId);

    List<PlanEditor> findByPlan_PlanId(String planId);

    boolean existsByUser_UserIdAndPlan_PlanId(String userId, String planId);
    boolean existsByUserUserIdAndPlanPlanId(String userId, String planId);
}
