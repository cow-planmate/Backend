package com.example.planmate.domain.register.dto;

import com.example.planmate.common.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse extends CommonResponse {
    private int userId;
}
