package com.example.planmate.service;

import com.example.planmate.dto.SearchDepartureResponse;
import org.springframework.stereotype.Service;

@Service
public class SearchDepartureService {
    public SearchDepartureResponse searchDeparture(String departureName) {
        SearchDepartureResponse response =  new SearchDepartureResponse();
        return response;
    }
}
