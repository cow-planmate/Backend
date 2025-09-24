package com.example.planmate.domain.shared.service.sync.ports;

public interface PlanQueryPort {
    boolean existsActivePlan(int planId);
}
