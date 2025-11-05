package com.example.planmate.domain.user.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeAgeRequest implements IRequest {
    private int age;
}
