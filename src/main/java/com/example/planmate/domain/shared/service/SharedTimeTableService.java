package com.example.planmate.domain.shared.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.planmate.domain.shared.cache.TimeTableCache;
import com.example.planmate.domain.shared.dto.WTimetableRequest;
import com.example.planmate.domain.shared.dto.WTimetableResponse;
import com.example.planmate.domain.shared.lazydto.TimeTableDto;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class SharedTimeTableService implements SharedService<WTimetableRequest, WTimetableResponse> {
    private final TimeTableCache timeTableCache;
    @Override
    public WTimetableResponse create(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> timeTableDtos = request.getTimeTableDtos();
        for(TimeTableDto timeTableDto : timeTableDtos) {
            // create에서는 무조건 ID를 null로 설정하여 백엔드에서 음수 ID 자동 생성
            TimeTableDto dtoWithNullId = timeTableDto.withTimeTableId(null);
            TimeTableDto tempTableDto = timeTableCache.save(dtoWithNullId);
            response.addTimetableVO(tempTableDto);
        }
        return response;
    }

    //접근 보안 필요 throw new AccessDeniedException("timetable 접근 권한이 없습니다");
    @Override
    public WTimetableResponse update(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> timeTableDtos = request.getTimeTableDtos();
        for(TimeTableDto timeTableDto : timeTableDtos) {
            TimeTableDto tempTableDto = timeTableCache.update(timeTableDto);
            response.addTimetableVO(tempTableDto);
        }
        return response;
    }
    
    @Override
    public WTimetableResponse delete(WTimetableRequest request) {
        WTimetableResponse response = new WTimetableResponse();
        List<TimeTableDto> timeTableDtos = request.getTimeTableDtos();
        for(TimeTableDto timeTableDto : timeTableDtos) {
            timeTableCache.deleteById(timeTableDto.timeTableId());
            response.addTimetableVO(timeTableDto);
        }
        return response;
    }


    
}
