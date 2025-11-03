package com.example.planmate.domain.shared.lazydto;

import java.time.LocalTime;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.shared.framework.annotation.AutoDatabaseLoader;
import com.example.planmate.domain.shared.framework.annotation.AutoEntityConverter;
import com.example.planmate.domain.shared.framework.annotation.AutoRedisTemplate;
import com.example.planmate.domain.shared.framework.annotation.CacheEntity;
import com.example.planmate.domain.shared.framework.annotation.CacheId;
import com.example.planmate.domain.shared.framework.annotation.EntityConverter;
import com.example.planmate.domain.shared.framework.annotation.ParentId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@CacheEntity
@AutoRedisTemplate("timeTablePlaceBlockRedis")
@AutoDatabaseLoader(repository = "timeTablePlaceBlockRepository", method = "findByTimeTableTimeTableId")
@AutoEntityConverter(repositories = {"placeCategoryRepository", "timeTableRepository", "placePhotoRepository"})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode
public class TimeTablePlaceBlockDto {

    @CacheId
    private Integer blockId;
    private String placeName;
    private String placeTheme;
    private float placeRating;
    private String placeAddress;
    private String placeLink;
    private LocalTime blockStartTime;
    private LocalTime blockEndTime;
    private double xLocation;
    private double yLocation;
    private String placeId;
    private Integer placeCategoryId;
    @ParentId
    private Integer timeTableId;

    public static TimeTablePlaceBlockDto fromEntity(TimeTablePlaceBlock block) {
        return TimeTablePlaceBlockDto.builder()
                .blockId(block.getBlockId())
                .placeName(block.getPlaceName())
                .placeTheme(block.getPlaceTheme())
                .placeRating(block.getPlaceRating())
                .placeAddress(block.getPlaceAddress())
                .placeLink(block.getPlaceLink())
                .blockStartTime(block.getBlockStartTime())
                .blockEndTime(block.getBlockEndTime())
                .xLocation(block.getXLocation())
                .yLocation(block.getYLocation())
                .placeId(block.getPlacePhoto() != null ? block.getPlacePhoto().getPlaceId() : null)
                .placeCategoryId(block.getPlaceCategory().getPlaceCategoryId())
                .timeTableId(block.getTimeTable().getTimeTableId())
                .build();
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

    public TimeTablePlaceBlockDto withBlockId(Integer newBlockId) {
        return this.toBuilder()
                .blockId(newBlockId)
                .build();
    }
}
