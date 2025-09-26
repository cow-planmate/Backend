package com.example.planmate.domain.shared.sync.ports;

public interface PlanQueryPort {
    boolean existsActivePlan(int planId);
}
