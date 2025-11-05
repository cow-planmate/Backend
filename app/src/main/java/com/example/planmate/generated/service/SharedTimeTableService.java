package com.example.planmate.generated.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.planmate.generated.cache.TimeTableCache;
import com.example.planmate.generated.dto.WTimetableRequest;
import com.example.planmate.generated.dto.WTimetableResponse;
import com.example.planmate.generated.lazydto.TimeTableDto;
import com.sharedsync.framework.shared.service.SharedService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SharedTimeTableService implements SharedService<WTimetableRequest, WTimetableResponse> {

    private final TimeTableCache timeTableCache;

    @Override
    public WTimetableResponse create(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> payload = request.getTimeTableDtos() != null ? request.getTimeTableDtos()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTableDtos(Collections.emptyList());
            return response;
        }

        List<TimeTableDto> sanitized = payload.stream()
                .map(dto -> dto.withTimeTableId(null))
                .collect(Collectors.toList());

        List<TimeTableDto> saved = timeTableCache.saveAll(sanitized);
        response.setTimeTableDtos(saved);
        return response;
    }

    @Override
    public WTimetableResponse read(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> payload = request.getTimeTableDtos() != null ? request.getTimeTableDtos()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTableDtos(Collections.emptyList());
            return response;
        }

        Integer parentPlanId = payload.stream()
                .map(TimeTableDto::planId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (parentPlanId == null) {
            response.setTimeTableDtos(Collections.emptyList());
            return response;
        }

        List<TimeTableDto> dtos = timeTableCache.findDtosByParentId(parentPlanId);
        response.setTimeTableDtos(dtos != null ? dtos : Collections.emptyList());
        return response;
    }

    @Override
    public WTimetableResponse update(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> payload = request.getTimeTableDtos() != null ? request.getTimeTableDtos()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTableDtos(Collections.emptyList());
            return response;
        }

        List<TimeTableDto> updated = payload.stream()
                .map(timeTableCache::update)
                .collect(Collectors.toList());

        response.setTimeTableDtos(updated);
        return response;
    }

    @Override
    public WTimetableResponse delete(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> payload = request.getTimeTableDtos() != null ? request.getTimeTableDtos()
                : Collections.emptyList();

        if (payload.isEmpty()) {
            response.setTimeTableDtos(Collections.emptyList());
            return response;
        }

        List<Integer> ids = payload.stream()
                .map(TimeTableDto::timeTableId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!ids.isEmpty()) {
            timeTableCache.deleteAllById(ids);
        }

        response.setTimeTableDtos(payload);
        return response;
    }
}
