package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.shared.framework.repository.AutoCacheRepository;
import com.example.planmate.domain.shared.lazydto.PlanDto;

@Component
public class PlanCache extends AutoCacheRepository<Plan, Integer, PlanDto> {
}
