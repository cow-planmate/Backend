package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.shared.framework.repository.SuperAutoCacheRepository;
import com.example.planmate.domain.shared.lazydto.PlanDto;

@Component
public class PlanCache extends SuperAutoCacheRepository<Plan, Integer, PlanDto> {
}
