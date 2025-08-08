package com.example.planmate.register.dto;

import com.example.planmate.dto.CommonResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameVerificationResponse extends CommonResponse {
    private boolean nicknameAvailable;
}
