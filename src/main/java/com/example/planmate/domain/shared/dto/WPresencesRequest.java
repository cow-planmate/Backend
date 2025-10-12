package com.example.planmate.domain.shared.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.example.planmate.domain.shared.framework.dto.WRequest;
import com.example.planmate.domain.shared.valueObject.UserDayIndexVO;

@Getter
@Setter
public class WPresencesRequest extends WRequest {
    private List<UserDayIndexVO> userDayIndexVO;

}
