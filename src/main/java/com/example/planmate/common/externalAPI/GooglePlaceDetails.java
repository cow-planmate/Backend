package com.example.planmate.common.externalAPI;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.example.planmate.common.valueObject.PlaceVO;

import lombok.RequiredArgsConstructor;

/**
 * Provides synchronous and aggregate asynchronous methods for fetching Google Place details (images & summary).
 */
@Component
@RequiredArgsConstructor
public class GooglePlaceDetails {

    private final GooglePlaceImageWorker googlePlaceImageWorker;

    /**
     * Triggers async image/detail fetching but does not wait for them.
     */
    public void fetchMissingImagesInBackground(List<? extends PlaceVO> placeVOs) {
        if (placeVOs == null || placeVOs.isEmpty()) return;
        
        placeVOs.stream()
            .filter(vo -> vo.getPhotoUrl() == null)
            .forEach(vo -> googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(vo.getPlaceId())
                .thenAccept(details -> {
                    if (details != null) {
                        vo.setPhotoUrl(details.photoUrl());
                    }
                }));
    }

    /**
     * Async bulk fetch that blocks until all individual async tasks complete.
     */
    public List<? extends PlaceVO> searchGooglePlaceDetailsAsyncBlocking(List<? extends PlaceVO> placeVOs) {
        if (placeVOs == null || placeVOs.isEmpty()) return placeVOs;

        List<CompletableFuture<Void>> futures = placeVOs.stream()
            .filter(vo -> vo.getPhotoUrl() == null)
            .map(vo -> googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(vo.getPlaceId())
                .thenAccept(details -> {
                    if (details != null) {
                        vo.setPhotoUrl(details.photoUrl());
                    }
                }))
            .toList();

        if (!futures.isEmpty()) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
        
        return placeVOs;
    }

    /**
     * Fully async aggregate method returning a CompletableFuture.
     */
    public CompletableFuture<List<? extends PlaceVO>> searchGooglePlaceDetailsAsync(List<? extends PlaceVO> placeVOs) {
        if (placeVOs == null || placeVOs.isEmpty()) {
            return CompletableFuture.completedFuture(placeVOs);
        }

        List<CompletableFuture<Void>> futures = placeVOs.stream()
            .filter(vo -> vo.getPhotoUrl() == null)
            .map(vo -> googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(vo.getPlaceId())
                .thenAccept(details -> {
                    if (details != null) {
                        vo.setPhotoUrl(details.photoUrl());
                    }
                }))
            .toList();
        
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture((List<PlaceVO>) placeVOs);
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> (List<PlaceVO>) placeVOs);
    }
}

