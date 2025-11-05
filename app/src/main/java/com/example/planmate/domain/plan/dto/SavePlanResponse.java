package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SavePlanResponse extends CommonResponse {
    private int planId;
}
