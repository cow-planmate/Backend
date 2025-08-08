package com.example.planmate.domain.departure.service;

import com.example.planmate.domain.departure.dto.SearchDepartureResponse;
import com.example.planmate.common.externalAPI.GoogleMap;
import com.example.planmate.common.valueObject.DepartureVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartureService {
    private final GoogleMap googleMap;
    public SearchDepartureResponse searchDeparture(String departureName) throws IOException {
        SearchDepartureResponse response =  new SearchDepartureResponse();
        List<DepartureVO> searchSuggestions = googleMap.searchDeparture(departureName);
        response.addDeparture(searchSuggestions);
        return response;
    }
}
