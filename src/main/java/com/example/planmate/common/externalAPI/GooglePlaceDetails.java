package com.example.planmate.common.externalAPI;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;

import lombok.RequiredArgsConstructor;

/**
 * Provides synchronous and aggregate asynchronous methods for fetching Google Place images.
 * Per-place asynchronous logic lives in {@link GooglePlaceImageWorker} to ensure @Async proxying.
 */
@Component
@RequiredArgsConstructor
public class GooglePlaceDetails {

    @Value("${api.google.key}")
    private String googleApiKey;

    @Value("${spring.img.url}")
    private String imgUrl;

    private final GooglePlaceImageWorker googlePlaceImageWorker;
    private final PlacePhotoRepository placePhotoRepository;

    /**
     * Async bulk fetch that blocks until all individual async tasks complete.
     */
    public List<? extends PlaceVO> searchGooglePlaceDetailsAsyncBlocking(List<? extends PlaceVO> placeVOs) {
        
        // placePhotoRepository에서 기존 데이터 먼저 조회
        List<String> placeIds = placeVOs.stream()
            .map(PlaceVO::getPlaceId)
            .toList();
        List<PlacePhoto> existingPhotos = placePhotoRepository.findAllById(placeIds);
        Map<String, PlacePhoto> existingPhotoMap = existingPhotos.stream()
            .collect(Collectors.toMap(PlacePhoto::getPlaceId, p -> p));
        
        // 기존에 없는 placeId만 API 호출
        List<? extends PlaceVO> missingPlaceVOs = placeVOs.stream()
            .filter(vo -> !existingPhotoMap.containsKey(vo.getPlaceId()))
            .toList();
        
        if (!missingPlaceVOs.isEmpty()) {
            List<CompletableFuture<PlacePhoto>> futures = missingPlaceVOs.stream()
                    .map(vo -> googlePlaceImageWorker.fetchSinglePlaceImageAsync(vo.getPlaceId()))
                    .collect(Collectors.toList());
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            List<PlacePhoto> newPhotos = futures.stream()
                .map(CompletableFuture::join)
                .filter(p -> p != null)
                .toList();
            
            if (!newPhotos.isEmpty()) {
                // Deduplicate by placeId and save new photos
                Map<String, PlacePhoto> unique = new LinkedHashMap<>();
                for (PlacePhoto p : newPhotos) {
                    unique.putIfAbsent(p.getPlaceId(), p);
                }
                List<PlacePhoto> toSave = unique.values().stream()
                    .filter(p -> !placePhotoRepository.existsById(p.getPlaceId()))
                    .toList();
                if (!toSave.isEmpty()) {
                    placePhotoRepository.saveAll(toSave);
                }
            }
        }
        
        return placeVOs;
    }

    /**
     * Fully async aggregate method returning a CompletableFuture.
     */
    public CompletableFuture<List<? extends PlaceVO>> searchGooglePlaceDetailsAsync(List<? extends PlaceVO> placeVOs) {
        // placePhotoRepository에서 기존 데이터 먼저 조회
        List<String> placeIds = placeVOs.stream()
            .map(PlaceVO::getPlaceId)
            .toList();
        List<PlacePhoto> existingPhotos = placePhotoRepository.findAllById(placeIds);
        Map<String, PlacePhoto> existingPhotoMap = existingPhotos.stream()
            .collect(Collectors.toMap(PlacePhoto::getPlaceId, p -> p));
        
        // 기존에 없는 placeId만 API 호출
        List<? extends PlaceVO> missingPlaceVOs = placeVOs.stream()
            .filter(vo -> !existingPhotoMap.containsKey(vo.getPlaceId()))
            .toList();
        
        if (missingPlaceVOs.isEmpty()) {
            // 모든 데이터가 이미 존재하면 바로 반환
            return CompletableFuture.completedFuture(placeVOs);
        }
        
        List<CompletableFuture<PlacePhoto>> futures = missingPlaceVOs.stream()
                .map(vo -> googlePlaceImageWorker.fetchSinglePlaceImageAsync(vo.getPlaceId()))
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> {
            List<PlacePhoto> newPhotos = futures.stream()
                .map(CompletableFuture::join)
                .filter(p -> p != null)
                .toList();
            if (!newPhotos.isEmpty()) {
                Map<String, PlacePhoto> unique = new LinkedHashMap<>();
                for (PlacePhoto p : newPhotos) {
                    unique.putIfAbsent(p.getPlaceId(), p);
                }
                List<PlacePhoto> toSave = unique.values().stream()
                    .filter(p -> !placePhotoRepository.existsById(p.getPlaceId()))
                    .toList();
                if (!toSave.isEmpty()) {
                    placePhotoRepository.saveAll(toSave);
                }
            }
            return placeVOs;
        });
    }
}
