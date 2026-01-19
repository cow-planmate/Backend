package com.example.planmate.domain.plan.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.common.valueObject.PlaceBlockVO;
import com.example.planmate.common.valueObject.PlanFrameVO;
import com.example.planmate.common.valueObject.TimetableVO;
// import com.example.planmate.domain.webSocket.valueObject.UserDayIndexVO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class GetPlanResponse extends CommonResponse {
    private PlanFrameVO planFrame;
    private List<PlaceBlockVO> placeBlocks;
    private List<TimetableVO> timetables;
    public GetPlanResponse() {
        placeBlocks = new ArrayList<>();
        timetables = new ArrayList<>();
    }
    public void addPlanFrame(int planId, String planName, String departure, String travelCategoryName, int travelId, String travelName, int adultCount, int childCount, int transportationCategoryId) {
        planFrame = new PlanFrameVO(planId, planName, departure, travelCategoryName, travelId, travelName, adultCount, childCount, transportationCategoryId);
    }
    public void addPlaceBlock(int blockId, int timeTableId, int placeCategory, String placeName, String placeTheme, float placeRating, String placeAddress, String placeLink, String placeId, double xLocation, double yLocation, LocalTime startTime, LocalTime endTime) {
        placeBlocks.add(new PlaceBlockVO(blockId, timeTableId, placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, placeId, xLocation, yLocation, startTime, endTime));
    }
    public void addTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        timetables.add(new TimetableVO(timetableId, date, startTime, endTime));
    }

}
