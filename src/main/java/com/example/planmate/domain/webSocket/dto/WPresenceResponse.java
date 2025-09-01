package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPresenceResponse extends WResponse {
    private UserDayIndexVO userDayIndexVO;
    public WPresenceResponse(String nickname, int dayIndex) {
        userDayIndexVO = new UserDayIndexVO(nickname, dayIndex);
    }
}
