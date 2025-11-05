package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetShareLinkResponse extends CommonResponse {
    private String sharedPlanUrl;
}
