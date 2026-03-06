package com.example.planmate.domain.travel.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.domain.travel.dto.GetTravelResponse;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.entity.TravelCategory;
import com.example.planmate.domain.travel.repository.TravelRepository;

@ExtendWith(MockitoExtension.class)
class TravelServiceTest {

    @Mock
    private TravelRepository travelRepository;

    @InjectMocks
    private TravelService travelService;

    @Test
    @DisplayName("getTravel: 전체 여행지 목록을 조회한다 (외부 API 미호출)")
    void getTravel() {
        // given
        TravelCategory category = mock(TravelCategory.class);
        given(category.getTravelCategoryId()).willReturn(1);
        given(category.getTravelCategoryName()).willReturn("Domestic");

        Travel travel = mock(Travel.class);
        given(travel.getTravelId()).willReturn(10);
        given(travel.getTravelName()).willReturn("Seoul");
        given(travel.getTravelCategory()).willReturn(category);

        given(travelRepository.findAll()).willReturn(List.of(travel));

        // when
        GetTravelResponse response = travelService.getTravel();

        // then
        assertNotNull(response);
        verify(travelRepository).findAll();
    }
}
