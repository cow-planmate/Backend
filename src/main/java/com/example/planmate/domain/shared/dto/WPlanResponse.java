package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.framework.dto.WResponse;
import com.example.planmate.domain.shared.lazydto.PlanDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanResponse extends WResponse {
    private PlanDto planDto;
}
