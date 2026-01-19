package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CreatePlanResponse extends CommonResponse {
    private int planId;
}
