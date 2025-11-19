package com.example.planmate.generated.dto;

import java.util.List;

import com.sharedsync.framework.shared.framework.dto.WResponse;
import com.example.planmate.generated.lazydto.TimeTablePlaceBlockDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WTimeTablePlaceBlockResponse extends WResponse {
    private List<TimeTablePlaceBlockDto> timeTablePlaceBlockDto;
}
