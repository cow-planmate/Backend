package com.example.planmate.domain.shared.service;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.shared.cache.TimeTablePlaceBlockCache;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.domain.shared.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedTimeTablePlaceBlockService implements SharedService<WTimeTablePlaceBlockRequest, WTimeTablePlaceBlockResponse> {
    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;
    
    @Override
    public WTimeTablePlaceBlockResponse create(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimeTablePlaceBlockDto timeTablePlaceBlockDto = request.getTimeTablePlaceBlockDto();
        TimeTablePlaceBlockDto tempTimeTablePlaceBlockDto = timeTablePlaceBlockCache.save(timeTablePlaceBlockDto);
        response.setTimeTablePlaceBlockDto(tempTimeTablePlaceBlockDto);
        return response;
    }

    @Override
    public WTimeTablePlaceBlockResponse update(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimeTablePlaceBlockDto timeTablePlaceBlockDto = request.getTimeTablePlaceBlockDto();
        TimeTablePlaceBlockDto tempTimeTablePlaceBlockDto = timeTablePlaceBlockCache.save(timeTablePlaceBlockDto);
        response.setTimeTablePlaceBlockDto(tempTimeTablePlaceBlockDto);
        return response;
    }

    @Override
    public WTimeTablePlaceBlockResponse delete(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        TimeTablePlaceBlockDto timeTablePlaceBlockDto = request.getTimeTablePlaceBlockDto();
        timeTablePlaceBlockCache.deleteById(timeTablePlaceBlockDto.blockId());
        response.setTimeTablePlaceBlockDto(timeTablePlaceBlockDto);
        return response;
    }

}
