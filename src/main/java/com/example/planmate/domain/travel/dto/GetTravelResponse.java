package com.example.planmate.domain.travel.dto;

import com.example.planmate.common.dto.CommonResponse;
import com.example.planmate.domain.travel.entity.Travel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class GetTravelResponse extends CommonResponse {
    private List<Travel> travels;
    public GetTravelResponse(){
        travels = new ArrayList<>();
    }
    public void addTravel(Travel travel){
        travels.add(travel);
    }
}
