package com.example.planmate.domain.webSocket.dto;

import com.example.planmate.common.valueObject.TimetableVO;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WTimetableResponse{
    private final List<TimetableVO> timetableVOs;
    public WTimetableResponse() {
        timetableVOs = new ArrayList<>();
    }
    public void addTimetableVO(TimetableVO timetableVO) {
        timetableVOs.add(timetableVO);
    }

}
