package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class SavePlanRequest {
    private String departure;
    private int transportationCategoryId;
    private int adultCount;
    private int childCount;
    private List<TimetableVO> timetables;
    private List<List<TimetablePlaceBlockVO>> timetablePlaceBlocks;
}
