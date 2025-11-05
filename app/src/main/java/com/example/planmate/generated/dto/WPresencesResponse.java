package com.example.planmate.generated.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import com.sharedsync.framework.shared.framework.dto.WResponse;
import com.example.planmate.generated.valueObject.UserDayIndexVO;

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
