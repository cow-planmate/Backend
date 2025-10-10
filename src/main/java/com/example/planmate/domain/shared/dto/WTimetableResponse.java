package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.lazydto.TimeTableDto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class WTimetableResponse extends WResponse {
    private List<TimeTableDto> tableDtos;
    public WTimetableResponse() {
        tableDtos = new ArrayList<>();
    }
    public void addTimetableVO(TimeTableDto timeTableDto) {
        tableDtos.add(timeTableDto);
    }

}
