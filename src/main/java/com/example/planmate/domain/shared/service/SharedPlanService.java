package com.example.planmate.domain.shared.service;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.dto.WPlanRequest;
import com.example.planmate.domain.shared.dto.WPlanResponse;
import com.example.planmate.domain.shared.lazydto.PlanDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedPlanService implements SharedService<WPlanRequest, WPlanResponse> {
    private final PlanCache planCache;
    @Override
    public WPlanResponse update(WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        PlanDto planDto = request.getPlanDto();
        planCache.save(planDto); // JPA 스타일로 변경!
        return response;
    }

    @Override
    public WPlanResponse create(WPlanRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public WPlanResponse delete(WPlanRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
