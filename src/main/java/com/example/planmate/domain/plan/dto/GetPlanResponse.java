package com.example.planmate.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PlaceBlockVO;
import com.example.planmate.common.valueObject.PlanFrameVO;
import com.example.planmate.common.valueObject.TimetableVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "플랜 상세 조회 응답 데이터")
public class GetPlanResponse extends CommonResponse {
    @Schema(description = "플랜의 기본 정보(프레임)")
    private PlanFrameVO planFrame;

    @Schema(description = "플랜에 포함된 장소 블록 목록")
    private List<PlaceBlockVO> placeBlocks;

    @Schema(description = "플랜의 타임테이블(일차별) 목록")
    private List<TimetableVO> timetables;

    public GetPlanResponse() {
        placeBlocks = new ArrayList<>();
        timetables = new ArrayList<>();
    }
    public void addPlanFrame(int planId, String planName, String departure, String travelCategoryName, int travelId, String travelName, int adultCount, int childCount, int transportationCategoryId) {
        planFrame = new PlanFrameVO(planId, planName, departure, travelCategoryName, travelId, travelName, adultCount, childCount, transportationCategoryId);
    }
    public void addPlaceBlock(int blockId, int timeTableId, int placeCategory, String placeName, String placeTheme, Float placeRating, String placeAddress, String placeLink, String photoUrl, String placeId, Double xLocation, Double yLocation, LocalTime startTime, LocalTime endTime) {
        placeBlocks.add(new PlaceBlockVO(blockId, timeTableId, placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, photoUrl, placeId, xLocation, yLocation, startTime, endTime));
    }
    public void addTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        timetables.add(new TimetableVO(timetableId, date, startTime, endTime));
    }

}
