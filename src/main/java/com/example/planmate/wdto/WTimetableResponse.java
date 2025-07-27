package com.example.planmate.wdto;

import com.example.planmate.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimetableResponse extends WResponse {
    private TimetableVO timetableVO;
}
