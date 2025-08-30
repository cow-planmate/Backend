package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WPresencesResponse extends WResponse {
    private List<UserDayIndexVO> userDayIndexVOs;
    public WPresencesResponse() {
        userDayIndexVOs = new ArrayList<>();
    }
    public void addUserDayIndexVO(String nickname, int dayIndex) {
        userDayIndexVOs.add(new UserDayIndexVO(nickname, dayIndex));
    }
}
