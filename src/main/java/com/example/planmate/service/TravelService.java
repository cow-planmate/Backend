package com.example.planmate.service;

import com.example.planmate.dto.GetTravelResponse;
import com.example.planmate.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;

    public GetTravelResponse getTravel(){
        GetTravelResponse response = new GetTravelResponse();
        travelRepository.findAll().forEach(travel -> {
            response.addTravel(travel);
        });
        return response;
    }
}
