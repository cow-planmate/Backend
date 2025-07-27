package com.example.planmate.wdto;

import com.example.planmate.valueObject.TimetablePlaceBlockVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockRequest extends WRequest {
    private TimetablePlaceBlockVO timetablePlaceBlockVO;
}
