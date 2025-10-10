package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.lazydto.PlanDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanRequest extends WRequest {
    private PlanDto planDto;
}
