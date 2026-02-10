package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DeleteMultiplePlansRequest implements IRequest {
    private List<UUID> planIds;
}
