package com.example.planmate.wdto;

import com.example.planmate.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WTimetableRequest{
    private List<TimetableVO> timetableVOs;
}
