package com.example.planmate.domain.shared.dto;

import java.util.List;

import com.example.planmate.domain.shared.framework.dto.WResponse;
import com.example.planmate.domain.shared.lazydto.PlanDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanResponse extends WResponse {
    private List<PlanDto> planDto;
}
