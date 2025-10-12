package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.framework.dto.WResponse;
import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockResponse extends WResponse {
    private TimeTablePlaceBlockDto timeTablePlaceBlockDto;
}
