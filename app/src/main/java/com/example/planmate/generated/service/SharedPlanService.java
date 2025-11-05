package com.example.planmate.generated.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.planmate.generated.cache.PlanCache;
import com.example.planmate.generated.dto.WPlanRequest;
import com.example.planmate.generated.dto.WPlanResponse;
import com.example.planmate.generated.lazydto.PlanDto;
import com.sharedsync.framework.shared.service.SharedService;
import org.springframework.stereotype.Service;

import com.example.planmate.domain.plan.entity.Plan;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedPlanService implements SharedService<WPlanRequest, WPlanResponse> {

    private final PlanCache planCache;

    @Override
    public WPlanResponse create(WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        List<PlanDto> payload = request.getPlanDto() != null ? request.getPlanDto() : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setPlanDto(Collections.emptyList());
            return response;
        }

        List<PlanDto> sanitized = payload.stream()
                .map(dto -> dto.withPlanId(null))
                .collect(Collectors.toList());

        List<PlanDto> saved = planCache.saveAll(sanitized);
        response.setPlanDto(saved);
        return response;
    }

    @Override
    public WPlanResponse read(WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        List<PlanDto> payload = request.getPlanDto() != null ? request.getPlanDto() : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setPlanDto(Collections.emptyList());
            return response;
        }

        List<Integer> ids = payload.stream()
                .map(PlanDto::planId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            response.setPlanDto(Collections.emptyList());
            return response;
        }

        List<Plan> entities = planCache.findAllById(ids);
        List<PlanDto> dtos = entities.stream()
                .filter(Objects::nonNull)
                .map(PlanDto::fromEntity)
                .collect(Collectors.toList());

        response.setPlanDto(dtos);
        return response;
    }

    @Override
    public WPlanResponse update(WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        List<PlanDto> payload = request.getPlanDto() != null ? request.getPlanDto() : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setPlanDto(Collections.emptyList());
            return response;
        }

        List<PlanDto> updated = payload.stream()
                .map(planCache::update)
                .collect(Collectors.toList());

        response.setPlanDto(updated);
        return response;
    }

    @Override
    public WPlanResponse delete(WPlanRequest request) {
        WPlanResponse response = new WPlanResponse();
        List<PlanDto> payload = request.getPlanDto() != null ? request.getPlanDto() : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setPlanDto(Collections.emptyList());
            return response;
        }

        List<Integer> ids = payload.stream()
                .map(PlanDto::planId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!ids.isEmpty()) {
            planCache.deleteAllById(ids);
        }

        response.setPlanDto(payload);
        return response;
    }
}
