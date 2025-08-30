package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WPresencesRequest extends WRequest {
    private List<UserDayIndexVO> userDayIndexVO;

}
