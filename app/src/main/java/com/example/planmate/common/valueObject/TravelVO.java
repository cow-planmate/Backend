package com.example.planmate.common.valueObject;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TravelVO {
    private int travelId;
    private String travelName;
    private int travelCategoryId;
    private String travelCategoryName;
}
