package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockRequest extends WRequest {
    private TimetablePlaceBlockVO timetablePlaceBlockVO;
}
