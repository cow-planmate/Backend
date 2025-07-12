package com.example.planmate.valueObject;

import lombok.Getter;

@Getter
public class DepartureVO {
    private String departureName;
    private String departureAddress;

    public DepartureVO (String departureName, String departureAddress){
        this.departureName = departureName;
        this.departureAddress = departureAddress;
    }
}
