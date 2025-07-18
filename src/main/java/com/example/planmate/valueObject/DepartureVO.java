package com.example.planmate.valueObject;

import lombok.Getter;

@Getter
public class DepartureVO {
    private String placeId;
    private String url;
    private String departureName;
    private String departureAddress;

    public DepartureVO (String placeId, String url, String departureName, String departureAddress){
        this.placeId = placeId;
        this.url = url;
        this.departureName = departureName;
        this.departureAddress = departureAddress;
    }
}
