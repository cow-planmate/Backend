package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.common.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WTimetableRequest extends WRequest {
    private List<TimetableVO> timetableVOs;
}
