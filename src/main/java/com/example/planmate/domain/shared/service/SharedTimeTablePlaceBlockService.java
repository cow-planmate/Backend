package com.example.planmate.domain.shared.service;

import org.checkerframework.checker.units.qual.t;
import org.springframework.stereotype.Service;

import com.example.planmate.common.valueObject.TimetablePlaceBlockVO;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.plan.entity.TimeTablePlaceBlock;
import com.example.planmate.domain.shared.cache.PlaceCategoryCache;
import com.example.planmate.domain.shared.cache.PlanCache;
import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockResponse;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SharedTimeTablePlaceBlockService {

    private final PlanCache planCache;
    private final TimeTableCache timeTableCache;
    private final PlacePhotoRepository placePhotoRepository;
    private final PlaceCategoryCache placeCategoryCache;
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;

    public WTimeTablePlaceBlockResponse createTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        TimeTablePlaceBlock timeTablePlaceBlock = TimeTablePlaceBlock.builder()
                .timeTable(timeTableCache.findTimeTableByTimeTableId(timetablePlaceBlockVO.getTimetableId()))
                .placeName(timetablePlaceBlockVO.getPlaceName())
                .placeTheme("")
                .placeRating(timetablePlaceBlockVO.getPlaceRating())
                .placeAddress(timetablePlaceBlockVO.getPlaceAddress())
                .placeLink(timetablePlaceBlockVO.getPlaceLink())
                .placePhoto(placePhotoRepository.getReferenceById(timetablePlaceBlockVO.getPlaceId()))
                .blockStartTime(timetablePlaceBlockVO.getStartTime())
                .blockEndTime(timetablePlaceBlockVO.getEndTime())
                .xLocation(timetablePlaceBlockVO.getXLocation())
                .yLocation(timetablePlaceBlockVO.getYLocation())
                .placeCategory(placeCategoryCache.findPlaceCategoryByPlaceCategoryId(timetablePlaceBlockVO.getPlaceCategoryId()))
                .build();
        int tempId = timeTablePlaceBlockCache.createTimeTablePlaceBlock(timetablePlaceBlockVO.getTimetableId(), timeTablePlaceBlock);
        timetablePlaceBlockVO.setTimetablePlaceBlockId(tempId);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }

    public WTimeTablePlaceBlockResponse updateTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimetablePlaceBlockVO timetablePlaceBlockVO = request.getTimetablePlaceBlockVO();
        if(timetablePlaceBlockVO.getTimetablePlaceBlockId() == null) {
            return response;
        }
        TimeTablePlaceBlock timetablePlaceBlock = timeTablePlaceBlockCache.findTimeTablePlaceBlockByBlockId(timetablePlaceBlockVO.getTimetablePlaceBlockId());
        if (timetablePlaceBlockVO.getPlaceName() != null) {
            timetablePlaceBlock.changePlaceName(timetablePlaceBlockVO.getPlaceName());
        }

        if (timetablePlaceBlockVO.getPlaceRating() != null) {
            timetablePlaceBlock.changeRating(timetablePlaceBlockVO.getPlaceRating());
        }
        if (timetablePlaceBlockVO.getPlaceAddress() != null) {
            timetablePlaceBlock.changeAddress(timetablePlaceBlockVO.getPlaceAddress());
        }
        if (timetablePlaceBlockVO.getPlaceLink() != null) {
            timetablePlaceBlock.changeLink(timetablePlaceBlockVO.getPlaceLink());
        }
        if (timetablePlaceBlockVO.getStartTime() != null || timetablePlaceBlockVO.getEndTime() != null) {
            timetablePlaceBlock.changeTimes(
                    timetablePlaceBlockVO.getStartTime() != null ? timetablePlaceBlockVO.getStartTime() : timetablePlaceBlock.getBlockStartTime(),
                    timetablePlaceBlockVO.getEndTime() != null ? timetablePlaceBlockVO.getEndTime() : timetablePlaceBlock.getBlockEndTime()
            );
        }
        if (timetablePlaceBlockVO.getXLocation() != null || timetablePlaceBlockVO.getYLocation() != null) {
            timetablePlaceBlock.changeLocation(
                    timetablePlaceBlockVO.getXLocation() != null ? timetablePlaceBlockVO.getXLocation() : timetablePlaceBlock.getXLocation(),
                    timetablePlaceBlockVO.getYLocation() != null ? timetablePlaceBlockVO.getYLocation() : timetablePlaceBlock.getYLocation()
            );
        }
        if (timetablePlaceBlockVO.getPlaceCategoryId() != null) {
            timetablePlaceBlock.changeCategory(placeCategoryCache.findPlaceCategoryByPlaceCategoryId(timetablePlaceBlockVO.getPlaceCategoryId()));
        }
        timeTablePlaceBlockCache.updateTimeTablePlaceBlock(timetablePlaceBlock);
        response.setTimetablePlaceBlockVO(timetablePlaceBlockVO);
        return response;
    }
    
    public WTimeTablePlaceBlockResponse deleteTimetablePlaceBlock(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        if(request.getTimetablePlaceBlockVO()!=null) {
            timeTablePlaceBlockCache.deleteTimeTablePlaceBlock(request.getTimetablePlaceBlockVO().getTimetableId(), request.getTimetablePlaceBlockVO().getTimetablePlaceBlockId());
            response.setTimetablePlaceBlockVO(request.getTimetablePlaceBlockVO());
        }
        return response;
    }

}
