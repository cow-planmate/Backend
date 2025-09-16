package com.example.planmate.domain.webSocket.lazydto;

import java.time.LocalTime;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;

public record TimeTablePlaceBlockDto(
        Integer blockId,
        String placeName,
        String placeTheme,
        float placeRating,
        String placeAddress,
        String placeLink,
        LocalTime blockStartTime,
        LocalTime blockEndTime,
        double xLocation,
        double yLocation,
        String placeId,
        Integer placeCategoryId,
        Integer timeTableId
) {
    public static TimeTablePlaceBlockDto fromEntity(TimeTablePlaceBlock block) {
        return new TimeTablePlaceBlockDto(
                block.getBlockId(),
                block.getPlaceName(),
                block.getPlaceTheme(),
                block.getPlaceRating(),
                block.getPlaceAddress(),
                block.getPlaceLink(),
                block.getBlockStartTime(),
                block.getBlockEndTime(),
                block.getXLocation(),
                block.getYLocation(),
                block.getPlacePhoto() != null ? block.getPlacePhoto().getPlaceId() : null,
                block.getPlaceCategory().getPlaceCategoryId(),
                block.getTimeTable().getTimeTableId()
        );
    }

        public TimeTablePlaceBlock toEntity(PlaceCategory placeCategory, TimeTable timeTable, PlacePhoto placePhoto) {
        return TimeTablePlaceBlock.builder()
                .blockId(this.blockId)
                .placeName(this.placeName)
                .placeTheme(this.placeTheme)
                .placeRating(this.placeRating)
                .placeAddress(this.placeAddress)
                .placeLink(this.placeLink)
                .blockStartTime(this.blockStartTime)
                .blockEndTime(this.blockEndTime)
                .xLocation(this.xLocation)
                .yLocation(this.yLocation)
                .placePhoto(placePhoto)
                .placeCategory(placeCategory)
                .timeTable(timeTable)
                .build();
    }
}
