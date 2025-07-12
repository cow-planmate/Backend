package com.example.planmate.dto;

import com.example.planmate.valueObject.PlaceBlockVO;
import com.example.planmate.valueObject.PlanFrameVO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GetPlanResponse extends CommonResponse{
    private List<PlanFrameVO> planFrames;
    private List<PlaceBlockVO> placeBlocks;
    public GetPlanResponse() {
        planFrames = new ArrayList<>();
        placeBlocks = new ArrayList<>();
    }
    public void addPlanFrame(int planId, String planName, String departure, String travel, int adultCount, int childCount, List<LocalDate> dates, int transportation) {
        planFrames.add(new PlanFrameVO(planId, planName, departure, travel, adultCount, childCount, dates, transportation));
    }
    public void addPlaceBlock(int placeCategory, String placeName, String placeTheme, float placeRating, String placeAddress, String placeLink, double xLocation, double yLocation) {
        placeBlocks.add(new PlaceBlockVO(placeCategory, placeName, placeTheme, placeRating, placeAddress, placeLink, xLocation, yLocation));
    }
}
