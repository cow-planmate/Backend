package com.example.planmate.generated.dto;

import com.sharedsync.framework.shared.framework.dto.WRequest;
import com.example.planmate.generated.valueObject.UserDayIndexVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WPresenceRequest extends WRequest {
    private UserDayIndexVO userDayIndexVO;
}
