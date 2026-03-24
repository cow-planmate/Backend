package com.example.planmate.domain.plan.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface PlanDetailPopulator {
    void addPlanFrame(UUID planId, String planName, String departure, String travelCategoryName,
                      int travelId, String travelName, int adultCount, int childCount, int transportationCategoryId);

    void addTimetable(int timetableId, LocalDate date, LocalTime startTime, LocalTime endTime);

    void addPlaceBlock(int blockId, int timeTableId, int placeCategory, String placeName, String placeTheme,
                       Float placeRating, String placeAddress, String placeLink, String photoUrl, String placeId,
                       Double xLocation, Double yLocation, LocalTime startTime, LocalTime endTime, String memo);
}
