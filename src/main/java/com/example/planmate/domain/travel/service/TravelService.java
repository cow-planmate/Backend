package com.example.planmate.domain.travel.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.domain.travel.dto.GetTravelResponse;
import com.example.planmate.domain.travel.repository.TravelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {
    private final TravelRepository travelRepository;

    @Transactional(readOnly = true)
    public GetTravelResponse getTravel() {
        GetTravelResponse response = new GetTravelResponse();

        travelRepository.findAll().forEach(travel -> {
            response.addTravel(
                    travel.getTravelId(),
                    travel.getTravelName(),
                    travel.getTravelCategory().getTravelCategoryId(),
                    travel.getTravelCategory().getTravelCategoryName()
            );
        });

        return response;
    }

}
