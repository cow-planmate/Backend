package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeAgeRequest implements IRequest {
    private int age;
}
