package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResponse {
    protected int errorCode = 200;
    protected String message;
    protected String token;

}
