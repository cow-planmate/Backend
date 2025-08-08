package com.example.planmate.register.dto;

import com.example.planmate.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse extends CommonResponse {
    private int userId;
}
