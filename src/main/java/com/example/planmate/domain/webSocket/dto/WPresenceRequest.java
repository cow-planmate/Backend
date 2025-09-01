package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPresenceRequest extends WRequest {
    private UserDayIndexVO userDayIndexVO;
}
