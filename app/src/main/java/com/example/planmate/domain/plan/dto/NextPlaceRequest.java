package com.example.planmate.domain.plan.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class NextPlaceRequest extends PlaceRequest {
    private List<String> nextPageTokens;
}
