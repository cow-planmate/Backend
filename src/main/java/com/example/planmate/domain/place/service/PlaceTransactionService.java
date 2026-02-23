package com.example.planmate.domain.place.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.domain.place.entity.PlaceSearchCondition;
import com.example.planmate.domain.place.entity.PlaceSearchResult;
import com.example.planmate.domain.place.repository.PlaceSearchConditionRepository;
import com.example.planmate.domain.place.repository.PlaceSearchResultRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceTransactionService {

    private final PlaceSearchConditionRepository placeSearchConditionRepository;
    private final PlaceSearchResultRepository placeSearchResultRepository;

    @Transactional(readOnly = true)
    public Optional<PlaceSearchCondition> getValidCondition(String cacheKey) {
        return placeSearchConditionRepository.findByCacheKey(cacheKey)
                .filter(condition -> condition.getExpiredAt().isAfter(LocalDateTime.now()));
    }

    @Transactional(readOnly = true)
    public List<PlaceSearchResult> getCachedResults(PlaceSearchCondition condition, int startOrder, int endOrder) {
        return placeSearchResultRepository.findByConditionAndSortOrderBetween(condition, startOrder, endOrder);
    }

    @Transactional(readOnly = true)
    public int getMaxSortOrder(PlaceSearchCondition condition) {
        return placeSearchResultRepository.findMaxSortOrderByCondition(condition);
    }

    public PlaceSearchCondition saveSearchCondition(int travelId, int categoryId, Integer targetThemeId,
            String cacheKey) {
        placeSearchConditionRepository.upsertCondition(
                travelId,
                categoryId,
                targetThemeId,
                cacheKey,
                LocalDateTime.now().plusDays(360));

        return placeSearchConditionRepository.findByCacheKey(cacheKey)
                .orElseThrow(() -> new RuntimeException("Condition should exist after upsert"));
    }

    public void clearOldResults(PlaceSearchCondition condition) {
        placeSearchResultRepository.deleteAllByCondition(condition);
    }

    public void saveSearchResults(PlaceSearchCondition condition, List<? extends PlaceVO> detailed,
            int startSortOrder) {
        List<PlaceSearchResult> resultsToSave = new ArrayList<>();
        int currentSortOrder = startSortOrder;

        for (PlaceVO vo : detailed) {
            float rating = vo.getRating();
            if (rating < 4.0f)
                continue;

            if (vo.getPhotoUrl() == null || vo.getPhotoUrl().isBlank()) {
                placeSearchResultRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(vo.getPlaceId())
                        .ifPresent(existing -> vo.setPhotoUrl(existing.getPhotoUrl()));
            }

            resultsToSave.add(PlaceSearchResult.builder()
                    .condition(condition)
                    .placeId(vo.getPlaceId())
                    .placeName(vo.getName())
                    .placeAddress(vo.getFormatted_address())
                    .placeRating(BigDecimal.valueOf(rating))
                    .photoUrl(vo.getPhotoUrl())
                    .iconUrl(vo.getIconUrl())
                    .placeLink(vo.getUrl())
                    .xLocation(vo.getXLocation())
                    .yLocation(vo.getYLocation())
                    .sortOrder(currentSortOrder++)
                    .build());
        }

        if (!resultsToSave.isEmpty()) {
            placeSearchResultRepository.saveAll(resultsToSave);
        }
    }

    public void updatePhotoUrlIfMissing(List<? extends PlaceVO> places) {
        if (places == null || places.isEmpty())
            return;

        for (PlaceVO vo : places) {
            if (vo.getPhotoUrl() == null || vo.getPhotoUrl().isBlank()) {
                placeSearchResultRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(vo.getPlaceId())
                        .ifPresent(existing -> {
                            vo.setPhotoUrl(existing.getPhotoUrl());
                            try {
                                placeSearchResultRepository.updatePhotoUrlByPlaceId(vo.getPlaceId(),
                                        existing.getPhotoUrl());
                            } catch (Exception e) {
                            }
                        });
            }
        }
    }
}
