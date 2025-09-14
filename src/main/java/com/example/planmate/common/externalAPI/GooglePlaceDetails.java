package com.example.planmate.common.externalAPI;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.planmate.common.valueObject.PlaceVO;
import com.example.planmate.domain.plan.entity.PlacePhoto;
import com.example.planmate.domain.plan.repository.PlacePhotoRepository;

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
    List<CompletableFuture<PlacePhoto>> futures = placeVOs.stream()
                .map(googlePlaceImageWorker::fetchSinglePlaceImageAsync)
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    List<PlacePhoto> photos = futures.stream()
        .map(CompletableFuture::join)
        .filter(p -> p != null)
        .toList();
    if (!photos.isEmpty()) {
        placePhotoRepository.saveAll(photos);
    }
        return placeVOs;
    }

    /**
     * Fully async aggregate method returning a CompletableFuture.
     */
    public CompletableFuture<List<? extends PlaceVO>> searchGooglePlaceDetailsAsync(List<? extends PlaceVO> placeVOs) {
    List<CompletableFuture<PlacePhoto>> futures = placeVOs.stream()
                .map(googlePlaceImageWorker::fetchSinglePlaceImageAsync)
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> {
            List<PlacePhoto> photos = futures.stream()
                .map(CompletableFuture::join)
                .filter(p -> p != null)
                .toList();
            if (!photos.isEmpty()) {
            placePhotoRepository.saveAll(photos);
            }
            return placeVOs;
        });
    }
}
