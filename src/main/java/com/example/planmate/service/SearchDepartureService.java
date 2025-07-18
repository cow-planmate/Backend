package com.example.planmate.service;

import com.example.planmate.dto.SearchDepartureResponse;
import com.example.planmate.externalAPI.GoogleMap;
import com.example.planmate.valueObject.DepartureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchDepartureService {
    private GoogleMap googleMap;
    public SearchDepartureResponse searchDeparture(String departureName) throws IOException {
        SearchDepartureResponse response =  new SearchDepartureResponse();
        List<DepartureVO> searchSuggestions = googleMap.searchDeparture(departureName);
        return response;
    }
}
