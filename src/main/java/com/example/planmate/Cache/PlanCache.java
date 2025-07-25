package com.example.planmate.Cache;

import com.example.planmate.entity.Plan;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class PlanCache {
    private final Map<Integer, Plan> cache = new ConcurrentHashMap<>();

    public Plan getPlan(int planId, Supplier<Plan> fetchFromDb) {
        return cache.computeIfAbsent(planId, id -> fetchFromDb.get());
    }
}
