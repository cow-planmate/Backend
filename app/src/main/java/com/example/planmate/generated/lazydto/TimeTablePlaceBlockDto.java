package com.example.planmate.generated.lazydto;

import java.time.LocalTime;

import com.example.planmate.domain.plan.entity.TimeTable;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.sharedsync.framework.shared.framework.annotation.AutoDatabaseLoader;
import com.sharedsync.framework.shared.framework.annotation.AutoRedisTemplate;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.ParentId;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String placeId;
    private Integer placeCategoryId;
    @ParentId(TimeTable.class)
    private Integer timeTableId;

    public static TimeTablePlaceBlockDto fromEntity(TimeTablePlaceBlock block) {
        return instantiateFromEntity(block, TimeTablePlaceBlockDto.class);
    }

}
