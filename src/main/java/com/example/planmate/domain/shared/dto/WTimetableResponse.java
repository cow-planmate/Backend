package com.example.planmate.domain.shared.dto;

import com.example.planmate.common.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class WTimetableResponse extends WResponse {
    private List<TimetableVO> timetableVOs;
    public WTimetableResponse() {
        timetableVOs = new ArrayList<>();
    }
    public void addTimetableVO(TimetableVO timetableVO) {
        timetableVOs.add(timetableVO);
    }
    public void sortTimetableVOs() {
        timetableVOs.sort(Comparator.comparing(TimetableVO::getDate));
    }

}
