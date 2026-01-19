package com.example.planmate.domain.plan.dto;
import java.util.List;

import com.example.planmate.common.valueObject.PlanFrameVO;
import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class CreatePlanRequest {
    private PlanFrameVO planFrame;
    private List<TimetableVO> timetables;
    private List<TimetablePlaceBlockVO> timetablePlaceBlocks;
}
