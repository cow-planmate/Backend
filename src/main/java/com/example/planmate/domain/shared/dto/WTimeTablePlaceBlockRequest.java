package com.example.planmate.domain.shared.dto;

import java.util.List;

import com.example.planmate.domain.shared.framework.dto.WRequest;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockRequest extends WRequest {
    private List<TimeTablePlaceBlockDto> timeTablePlaceBlockDto;
}
