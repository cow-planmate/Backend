package com.example.planmate.dto;

import com.example.planmate.valueObject.DepartureVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class SearchDepartureResponse extends CommonResponse{
    private List<DepartureVO> departures;
    public SearchDepartureResponse(){
        departures = new ArrayList<>();
    }
    public void addDeparture(int placeId, String url, String departureName, String departureAddress){
        departures.add(new DepartureVO(placeId, url, departureName, departureAddress));
    }
}
