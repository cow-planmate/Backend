package com.example.planmate.generated.dto;

import java.util.List;

import com.example.planmate.move.shared.framework.dto.WResponse;
import com.example.planmate.generated.lazydto.PlanDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanResponse extends WResponse {
    private List<PlanDto> planDto;
}
