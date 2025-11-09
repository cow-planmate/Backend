package com.example.planmate.generated.lazydto;

import java.time.LocalTime;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.sharedsync.framework.shared.framework.annotation.AutoDatabaseLoader;
import com.sharedsync.framework.shared.framework.annotation.AutoEntityConverter;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityConverter;
import com.sharedsync.framework.shared.framework.annotation.ParentId;
import com.sharedsync.framework.shared.framework.dto.CacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@AutoRedisTemplate("timeTablePlaceBlockRedis")
@AutoDatabaseLoader(repository = "timeTablePlaceBlockRepository", method = "findByTimeTableTimeTableId")
@AutoEntityConverter(repositories = {"placeCategoryRepository", "timeTableRepository", "placePhotoRepository"})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeTablePlaceBlockDto extends CacheDto<Integer> {

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
    @ParentId(TimeTable.class)
    private Integer timeTableId;

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

}
