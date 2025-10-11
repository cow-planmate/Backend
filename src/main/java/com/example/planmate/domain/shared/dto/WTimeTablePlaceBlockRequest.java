package com.example.planmate.domain.shared.dto;

import com.example.planmate.domain.shared.lazydto.TimeTablePlaceBlockDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockRequest extends WRequest {
    private TimeTablePlaceBlockDto timetablePlaceBlockDto;
}
