package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeGenderRequest implements IRequest {
    private int gender;
}
