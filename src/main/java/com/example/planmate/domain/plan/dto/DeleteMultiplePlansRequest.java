package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeleteMultiplePlansRequest implements IRequest {
    private List<Integer> planIds;
}
