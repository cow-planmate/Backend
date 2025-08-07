package com.example.planmate.wdto;

import com.example.planmate.valueObject.TimetableVO;
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
