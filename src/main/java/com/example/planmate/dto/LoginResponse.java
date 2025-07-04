package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse implements IResponse{
    private String message;
    private String token;
}
