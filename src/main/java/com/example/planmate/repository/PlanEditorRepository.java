package com.example.planmate.repository;

import com.example.planmate.entity.Plan;
import com.example.planmate.entity.PlanEditor;
import com.example.planmate.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanEditorRepository extends JpaRepository<PlanEditor, Integer> {

    boolean existsByUserAndPlan(User user, Plan plan);
}
