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
public class GetPlanResponse extends CommonResponse{
    private PlanFrameVO planFrame;
    private List<PlaceBlockVO> placeBlocks;
    private List<TimetableVO> timetables;
    public GetPlanResponse() {
        placeBlocks = new ArrayList<>();
        timetables = new ArrayList<>();
    }
    public void addPlanFrame(int planId, String planName, String departure, String travel, int adultCount, int childCount, int transportation) {
        planFrame = new PlanFrameVO(planId, planName, departure, travel, adultCount, childCount, transportation);
    }
    public void addPlaceBlock(int placeCategory, String placeName, String placeTheme, float placeRating, String placeAddress, String placeLink, double xLocation, double yLocation) {
        placeBlocks.add(new PlaceBlockVO(placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, xLocation, yLocation));
    }
    public void addTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        timetables.add(new TimetableVO(timetableId, date, startTime, endTime));
    }
}
