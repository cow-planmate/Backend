package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.framework.dto.WResponse;
import com.example.planmate.domain.shared.valueObject.UserDayIndexVO;

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
