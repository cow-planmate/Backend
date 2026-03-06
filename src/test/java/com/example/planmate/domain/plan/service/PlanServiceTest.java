package com.example.planmate.domain.plan.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.domain.plan.auth.PlanAccessValidator;
import com.example.planmate.domain.plan.dto.DeletePlanResponse;
import com.example.planmate.domain.plan.dto.MakePlanResponse;
import com.example.planmate.domain.plan.entity.Plan;
import com.example.planmate.domain.plan.entity.TransportationCategory;
import com.example.planmate.domain.plan.repository.PlaceCategoryRepository;
import com.example.planmate.domain.plan.repository.PlanEditorRepository;
import com.example.planmate.domain.plan.repository.PlanRepository;
import com.example.planmate.domain.plan.repository.PlanShareRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;
import com.example.planmate.domain.plan.repository.TimeTableRepository;
import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.travel.repository.TravelRepository;
import com.example.planmate.domain.user.entity.User;
import com.example.planmate.domain.user.repository.UserRepository;
import com.sharedsync.shared.storage.PresenceStorage;

import sharedsync.cache.PlanCache;
import sharedsync.cache.TimeTableCache;
import sharedsync.cache.TimeTablePlaceBlockCache;

@ExtendWith(MockitoExtension.class)
class PlanServiceTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlanAccessValidator planAccessValidator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TimeTableRepository timeTableRepository;
    @Mock
    private TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;
    @Mock
    private TransportationCategoryRepository transportationCategoryRepository;
    @Mock
    private TravelRepository travelRepository;
    @Mock
    private PlaceCategoryRepository placeCategoryRepository;
    @Mock
    private PlanEditorRepository planEditorRepository;
    @Mock
    private PlanShareRepository planShareRepository;
    @Mock
    private PlanCache planCache;
    @Mock
    private TimeTableCache timeTableCache;
    @Mock
    private TimeTablePlaceBlockCache timeTablePlaceBlockCache;
    @Mock
    private PresenceStorage presenceStorage;

    @InjectMocks
    private PlanService planService;

    @Test
    @DisplayName("makeService: 새로운 플랜을 생성한다")
    void testMakeService() {
        // given
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);
        Travel travel = mock(Travel.class);
        given(travel.getTravelName()).willReturn("서울");
        TransportationCategory tc = mock(TransportationCategory.class);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(travelRepository.findById(1)).willReturn(Optional.of(travel));
        given(transportationCategoryRepository.findById(1)).willReturn(Optional.of(tc));

        Plan plan = mock(Plan.class);
        given(plan.getPlanId()).willReturn(UUID.randomUUID());
        given(planRepository.save(any(Plan.class))).willReturn(plan);

        // when
        MakePlanResponse response = planService.makeService(userId, "서울", 1, 1, List.of(LocalDate.now()), 2, 1);

        // then
        assertNotNull(response);
        verify(timeTableRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("makePlanName: 고유한 플랜 이름을 생성한다")
    void testMakePlanName() {
        // given
        Travel travel = mock(Travel.class);
        given(travel.getTravelName()).willReturn("제주");

        Plan plan = mock(Plan.class);
        given(plan.getPlanName()).willReturn("제주 1");

        given(planRepository.findAll()).willReturn(List.of(plan));

        // when
        String result = planService.makePlanName(travel);

        // then
        assertEquals("제주 2", result);
    }

    @Test
    @DisplayName("deletePlan: 일정을 삭제한다")
    void testDeletePlan() {
        // given
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        given(planRepository.existsById(planId)).willReturn(true);
        given(planRepository.existsByPlanIdAndUserUserId(planId, userId)).willReturn(true);

        // when
        DeletePlanResponse response = planService.deletePlan(userId, planId);

        // then
        assertEquals("일정을 삭제했습니다.", response.getMessage());
        verify(planRepository).deleteById(planId);
    }

    @Test
    @DisplayName("getPlan: 캐시에 데이터가 존재할 때 정상 반환")
    void testGetPlanCacheHit() {
        // given
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        Plan plan = mock(Plan.class);
        User user = mock(User.class);
        Travel travel = mock(Travel.class);
        TransportationCategory tc = mock(TransportationCategory.class);
        com.example.planmate.domain.travel.entity.TravelCategory travelCategory = mock(
                com.example.planmate.domain.travel.entity.TravelCategory.class);

        given(plan.getUser()).willReturn(user);
        given(user.getUserId()).willReturn(userId);
        given(plan.getTravel()).willReturn(travel);
        given(travel.getTravelCategory()).willReturn(travelCategory);
        given(plan.getTransportationCategory()).willReturn(tc);

        given(planCache.findById(planId)).willReturn(Optional.of(plan));
        doNothing().when(presenceStorage).waitForSync(planId.toString());

        // when & then
        assertDoesNotThrow(() -> {
            planService.getPlan(userId, planId);
        });

        verify(planCache).findById(planId);
    }
}
