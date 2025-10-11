package com.example.planmate.domain.shared.lazydto;

import java.time.LocalTime;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.shared.cache.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.cache.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.cache.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.cache.annotation.CacheEntity;
import com.example.planmate.domain.shared.cache.annotation.CacheId;
import com.example.planmate.domain.shared.cache.annotation.EntityConverter;
import com.example.planmate.domain.shared.cache.annotation.ParentId;

@CacheEntity // keyType 생략 -> 자동으로 "timetableplaceblock" 생성
@AutoRedisTemplate("timeTablePlaceBlockRedis")
@AutoDatabaseLoader(repository = "timeTablePlaceBlockRepository", method = "findByTimeTableTimeTableId")
@AutoEntityConverter(repositories = {"placeCategoryRepository", "timeTableRepository", "placePhotoRepository"})
public record TimeTablePlaceBlockDto(
        @CacheId
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
        @ParentId
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

    @EntityConverter
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

    /**
     * ID만 변경된 새로운 TimeTablePlaceBlockDto 객체를 생성합니다.
     * @param newBlockId 새로운 블록 ID
     * @return ID가 변경된 새로운 DTO 객체
     */
    public TimeTablePlaceBlockDto withBlockId(Integer newBlockId) {
        return new TimeTablePlaceBlockDto(
                newBlockId,
                this.placeName,
                this.placeTheme,
                this.placeRating,
                this.placeAddress,
                this.placeLink,
                this.blockStartTime,
                this.blockEndTime,
                this.xLocation,
                this.yLocation,
                this.placeId,
                this.placeCategoryId,
                this.timeTableId
        );
    }
}
