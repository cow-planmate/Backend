package com.example.planmate.generated.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.move.shared.framework.dto.WResponse;
import com.example.planmate.generated.lazydto.TimeTableDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimetableResponse extends WResponse {
    private List<TimeTableDto> timeTableDtos;
    public WTimetableResponse() {
        timeTableDtos = new ArrayList<>();
    }
    public void addTimetableVO(TimeTableDto timeTableDto) {
        timeTableDtos.add(timeTableDto);
    }

}
