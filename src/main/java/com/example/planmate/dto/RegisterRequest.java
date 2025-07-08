package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest implements IRequest {
    private String email;
    private String nickname;
    private String password;
    private int gender;
    private int age;

}
