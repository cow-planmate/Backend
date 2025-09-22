package com.example.planmate.domain.shared.dto;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockResponse extends WResponse {
    private TimetablePlaceBlockVO timetablePlaceBlockVO;
}
