package com.example.planmate.domain.shared.dto;

import java.util.List;

import com.example.planmate.domain.shared.framework.dto.WRequest;
import com.example.planmate.domain.shared.lazydto.PlanDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanRequest extends WRequest {
    private List<PlanDto> planDto;
}
