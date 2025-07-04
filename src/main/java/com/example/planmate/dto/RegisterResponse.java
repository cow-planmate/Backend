package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterResponse implements IResponse{

    private String message;
    private Long userId;

}
