package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.plan.entity.PlaceCategory;
import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.sharedsync.framework.shared.framework.annotation.*;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@CacheEntity
@AutoRedisTemplate("timeTablePlaceBlockRedis")
@AutoDatabaseLoader(repository = "timeTablePlaceBlockRepository", method = "findByTimeTableTimeTableId")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TimeTablePlaceBlockDto extends EntityBackedCacheDto<Integer, TimeTablePlaceBlock> {

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
    @EntityReference(repository = "placePhotoRepository", entityType = PlacePhoto.class, optional = false)
    private String placeId;
    @EntityReference(repository = "placeCategoryRepository", entityType = PlaceCategory.class, optional = false)
    private Integer placeCategoryId;
    @ParentId(TimeTable.class)
    @EntityReference(repository = "timeTableRepository", entityType = TimeTable.class, optional = false)
    private Integer timeTableId;

    public static TimeTablePlaceBlockDto fromEntity(TimeTablePlaceBlock block) {
        return instantiateFromEntity(block, TimeTablePlaceBlockDto.class);
    }

}
