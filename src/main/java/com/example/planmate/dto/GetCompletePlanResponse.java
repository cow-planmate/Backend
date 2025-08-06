package com.example.planmate.dto;

import com.example.planmate.valueObject.PlaceBlockVO;
import com.example.planmate.valueObject.PlanFrameVO;
import com.example.planmate.valueObject.TimetableVO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GetCompletePlanResponse extends CommonResponse{
    private PlanFrameVO planFrame;
    private List<PlaceBlockVO> placeBlocks;
    private List<TimetableVO> timetables;
    public GetCompletePlanResponse() {
        placeBlocks = new ArrayList<>();
        timetables = new ArrayList<>();
    }
    public void addPlanFrame(int planId, String planName, String departure, int travelId, String travel, int adultCount, int childCount, int transportation) {
        planFrame = new PlanFrameVO(planId, planName, departure, travelId, travel, adultCount, childCount, transportation);
    }
    public void addPlaceBlock(int blockId, int placeCategory, String placeName, String placeTheme, float placeRating, String placeAddress, String placeLink, double xLocation, double yLocation, LocalTime startTime, LocalTime endTime) {
        placeBlocks.add(new PlaceBlockVO(blockId, placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, xLocation, yLocation, startTime, endTime));
    }
    public void addTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        timetables.add(new TimetableVO(timetableId, date, startTime, endTime));
    }
}
