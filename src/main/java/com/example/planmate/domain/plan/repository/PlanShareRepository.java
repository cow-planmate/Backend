package com.example.planmate.domain.plan.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.plan.entity.PlanShare;

public interface PlanShareRepository extends JpaRepository<PlanShare, UUID> {

}
