package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NicknameVerificationResponse extends CommonResponse{
    private boolean nicknameAvailable;
}
