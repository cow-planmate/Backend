package com.example.planmate.domain.place.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;
import com.example.planmate.domain.place.repository.PlaceSearchConditionRepository;
import com.example.planmate.domain.place.repository.PlaceSearchResultRepository;

@ExtendWith(MockitoExtension.class)
class PlaceTransactionServiceTest {

    @Mock
    private PlaceSearchConditionRepository placeSearchConditionRepository;
    @Mock
    private PlaceSearchResultRepository placeSearchResultRepository;

    @InjectMocks
    private PlaceTransactionService placeTransactionService;

    @Test
    @DisplayName("유효한 캐시 조건 가져오기 성공")
    void getValidCondition_success() {
        // given
        String cacheKey = "cacheKey123";
        PlaceSearchCondition condition = PlaceSearchCondition.builder()
                .cacheKey(cacheKey)
                .expiredAt(LocalDateTime.now().plusDays(1))
                .build();
        given(placeSearchConditionRepository.findByCacheKey(cacheKey)).willReturn(Optional.of(condition));

        // when
        Optional<PlaceSearchCondition> result = placeTransactionService.getValidCondition(cacheKey);

        // then
        assertTrue(result.isPresent());
        assertEquals(cacheKey, result.get().getCacheKey());
    }

    @Test
    @DisplayName("캐시된 검색 결과 반환")
    void getCachedResults_success() {
        // given
        PlaceSearchCondition condition = PlaceSearchCondition.builder().build();
        PlaceSearchResult result1 = PlaceSearchResult.builder().placeName("Place 1").sortOrder(1).build();
        PlaceSearchResult result2 = PlaceSearchResult.builder().placeName("Place 2").sortOrder(2).build();
        given(placeSearchResultRepository.findByConditionAndSortOrderBetween(condition, 1, 2))
                .willReturn(List.of(result1, result2));

        // when
        List<PlaceSearchResult> results = placeTransactionService.getCachedResults(condition, 1, 2);

        // then
        assertEquals(2, results.size());
        assertEquals("Place 1", results.get(0).getPlaceName());
    }

    @Test
    @DisplayName("검색 조건 저장 로직 (upsert 후 다시 반환)")
    void saveSearchCondition_success() {
        // given
        String cacheKey = "testKey";
        PlaceSearchCondition condition = PlaceSearchCondition.builder().cacheKey(cacheKey).build();
        given(placeSearchConditionRepository.findByCacheKey(cacheKey)).willReturn(Optional.of(condition));

        // when
        PlaceSearchCondition savedResult = placeTransactionService.saveSearchCondition(1, 1, 1, cacheKey);

        // then
        assertNotNull(savedResult);
        verify(placeSearchConditionRepository).upsertCondition(eq(1), eq(1), eq(1), eq(cacheKey),
                any(LocalDateTime.class));
    }
}
