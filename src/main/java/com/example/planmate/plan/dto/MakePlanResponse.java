package com.example.planmate.plan.dto;

import com.example.planmate.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MakePlanResponse extends CommonResponse {
    private int planId;
}
