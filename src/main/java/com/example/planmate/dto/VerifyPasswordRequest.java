package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordRequest implements IRequest {
    private String password;
}
