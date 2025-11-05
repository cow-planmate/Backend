package com.example.planmate.generated.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.sharedsync.framework.shared.framework.repository.AutoCacheRepository;
import com.example.planmate.generated.lazydto.PlanDto;

@Component
public class PlanCache extends AutoCacheRepository<Plan, Integer, PlanDto> {
}
