package com.example.planmate.generated.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.sharedsync.framework.shared.framework.dto.WRequest;
import com.example.planmate.generated.valueObject.UserDayIndexVO;

@Getter
@Setter
public class WPresencesRequest extends WRequest {
    private List<UserDayIndexVO> userDayIndexVO;

}
