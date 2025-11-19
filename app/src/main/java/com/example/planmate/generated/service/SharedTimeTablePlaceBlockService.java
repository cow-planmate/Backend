package com.example.planmate.generated.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.planmate.generated.cache.TimeTablePlaceBlockCache;
import com.example.planmate.generated.dto.WTimeTablePlaceBlockRequest;
import com.example.planmate.generated.dto.WTimeTablePlaceBlockResponse;
import com.example.planmate.generated.lazydto.TimeTablePlaceBlockDto;
import com.sharedsync.framework.shared.service.SharedService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedTimeTablePlaceBlockService
        implements SharedService<WTimeTablePlaceBlockRequest, WTimeTablePlaceBlockResponse> {

    private final TimeTablePlaceBlockCache timeTablePlaceBlockCache;

    @Override
    public WTimeTablePlaceBlockResponse create(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        List<TimeTablePlaceBlockDto> payload = request.getTimeTablePlaceBlockDto() != null
                ? request.getTimeTablePlaceBlockDto()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTablePlaceBlockDto(Collections.emptyList());
            return response;
        }

        List<TimeTablePlaceBlockDto> sanitized = payload.stream()
            .map(dto -> dto.<TimeTablePlaceBlockDto>changeId(null))
                .collect(Collectors.toList());

        List<TimeTablePlaceBlockDto> saved = timeTablePlaceBlockCache.saveAll(sanitized);
        response.setTimeTablePlaceBlockDto(saved);
        return response;
    }

    @Override
    public WTimeTablePlaceBlockResponse read(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        List<TimeTablePlaceBlockDto> payload = request.getTimeTablePlaceBlockDto() != null
                ? request.getTimeTablePlaceBlockDto()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTablePlaceBlockDto(Collections.emptyList());
            return response;
        }

        Integer parentTimeTableId = payload.stream()
            .map(TimeTablePlaceBlockDto::getTimeTableId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (parentTimeTableId == null) {
            response.setTimeTablePlaceBlockDto(Collections.emptyList());
            return response;
        }

        List<TimeTablePlaceBlockDto> dtos = timeTablePlaceBlockCache.findDtosByParentId(parentTimeTableId);
        response.setTimeTablePlaceBlockDto(dtos != null ? dtos : Collections.emptyList());
        return response;
    }

    @Override
    public WTimeTablePlaceBlockResponse update(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        List<TimeTablePlaceBlockDto> payload = request.getTimeTablePlaceBlockDto() != null
                ? request.getTimeTablePlaceBlockDto()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTablePlaceBlockDto(Collections.emptyList());
            return response;
        }

        List<TimeTablePlaceBlockDto> updated = payload.stream()
                .map(timeTablePlaceBlockCache::update)
                .collect(Collectors.toList());

        response.setTimeTablePlaceBlockDto(updated);
        return response;
    }

    @Override
    public WTimeTablePlaceBlockResponse delete(WTimeTablePlaceBlockRequest request) {
        WTimeTablePlaceBlockResponse response = new WTimeTablePlaceBlockResponse();
        List<TimeTablePlaceBlockDto> payload = request.getTimeTablePlaceBlockDto() != null
                ? request.getTimeTablePlaceBlockDto()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTablePlaceBlockDto(Collections.emptyList());
            return response;
        }

        List<Integer> ids = payload.stream()
                    .map(TimeTablePlaceBlockDto::getBlockId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!ids.isEmpty()) {
            timeTablePlaceBlockCache.deleteAllById(ids);
        }

        response.setTimeTablePlaceBlockDto(payload);
        return response;
    }
}
