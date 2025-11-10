package com.example.planmate.generated.lazydto;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;
import com.sharedsync.framework.shared.framework.annotation.CacheEntity;
import com.sharedsync.framework.shared.framework.annotation.CacheId;
import com.sharedsync.framework.shared.framework.annotation.EntityReference;
import com.sharedsync.framework.shared.framework.dto.EntityBackedCacheDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@CacheEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TravelDto extends EntityBackedCacheDto<Integer, Travel> {
    @CacheId
    private Integer travelId;
    private String travelName;
    @EntityReference(repository = "travelCategoryRepository", entityType = TravelCategory.class, optional = false)
    private Integer travelCategoryId;

    public static TravelDto fromEntity(Travel travel) {
        return instantiateFromEntity(travel, TravelDto.class);
    }
}