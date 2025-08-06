package com.example.planmate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InviteUserToPlanRequest implements IRequest {
    private String receiverNickname;
}
