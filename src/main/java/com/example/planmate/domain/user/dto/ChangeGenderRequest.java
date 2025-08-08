package com.example.planmate.domain.user.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeGenderRequest implements IRequest {
    private int gender;
}
