package com.example.planmate.departure.dto;

import com.example.planmate.dto.CommonResponse;
import com.example.planmate.valueObject.DepartureVO;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class SearchDepartureResponse extends CommonResponse {
    private List<DepartureVO> departures;
    public SearchDepartureResponse(){
        departures = new ArrayList<>();
    }
    public void addDeparture(String placeId, String url, String departureName, String departureAddress){
        departures.add(new DepartureVO(placeId, url, departureName, departureAddress));
    }
    public void addDeparture(List<DepartureVO> departures){

            for(DepartureVO departure : departures){
                if(departure != null) {
                    this.departures.add(new DepartureVO(departure.getPlaceId(), departure.getUrl(), departure.getDepartureName(), departure.getDepartureAddress()));
                }
            }
    }
}
