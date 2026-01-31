package com.example.planmate.domain.plan.dto;
import java.util.List;

import com.example.planmate.common.valueObject.PlanFrameVO;
import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.common.valueObject.TimetableVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "플랜 전체 상세 생성/수정 요청 데이터")
public class CreatePlanRequest {
    @Schema(description = "플랜의 기본 정보(프레임)")
    private PlanFrameVO planFrame;

    @Schema(description = "타임테이블(일차별) 목록")
    private List<TimetableVO> timetables;

    @Schema(description = "타임테이블 내 장소 블록 목록")
    private List<TimetablePlaceBlockVO> timetablePlaceBlocks;
}
