package com.example.planmate.generated.dto;

import java.util.List;

import com.example.planmate.move.shared.framework.dto.WRequest;
import com.example.planmate.generated.lazydto.PlanDto;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPlanRequest extends WRequest {
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<PlanDto> planDto;
}
