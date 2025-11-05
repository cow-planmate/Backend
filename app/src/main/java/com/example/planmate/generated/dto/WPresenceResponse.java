package com.example.planmate.generated.dto;

import com.sharedsync.framework.shared.framework.dto.WResponse;
import com.example.planmate.generated.valueObject.UserDayIndexVO;

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
