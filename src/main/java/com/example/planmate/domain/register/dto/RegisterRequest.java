package com.example.planmate.domain.register.dto;

import com.example.planmate.common.dto.IRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest implements IRequest {
    private String nickname;
    private String password;
    private int gender;
    private int age;

}
