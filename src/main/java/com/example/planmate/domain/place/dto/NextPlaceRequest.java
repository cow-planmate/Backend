package com.example.planmate.domain.place.dto;

import java.util.List;

import com.example.planmate.common.valueObject.NextPageTokenVO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NextPlaceRequest {
    private List<NextPageTokenVO> tokens;
}
