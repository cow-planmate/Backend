package com.example.planmate.dto;

import com.example.planmate.entity.Travel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class GetTravelResponse extends CommonResponse {
    private List<Travel> travels;
    public GetTravelResponse(){
        travels = new ArrayList<>();
    }
    public void addTravel(Travel travel){
        travels.add(travel);
    }
}
