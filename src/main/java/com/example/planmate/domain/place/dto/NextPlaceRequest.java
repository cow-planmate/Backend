package com.example.planmate.domain.place.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NextPlaceRequest {
    private List<String> tokens;
}
