package com.example.planmate.domain.image.service;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.planmate.common.externalAPI.GooglePlaceImageWorker;
import com.example.planmate.domain.place.repository.PlaceSearchResultRepository;
import com.example.planmate.domain.plan.repository.TimeTablePlaceBlockRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final GooglePlaceImageWorker googlePlaceImageWorker;
    private final ImageStorageInterface imageStorage;
    private final PlaceSearchResultRepository placeSearchResultRepository;
    private final TimeTablePlaceBlockRepository timeTablePlaceBlockRepository;

    @Transactional
    public ResponseEntity<Resource> getGooglePlaceImage(String placeId) {
        if (!StringUtils.hasText(placeId)) {
            return ResponseEntity.badRequest().build();
        }

        // 1. Try to find photoUrl in current database records
        String photoUrl = findPhotoUrlInDb(placeId);

        // If explicitly set to empty string, it means "no photo available" - return 404 immediately
        if ("".equals(photoUrl)) {
            return ResponseEntity.notFound().build();
        }

        // 2. If not found in DB (null), try to trigger a fetch
        if (photoUrl == null) {
            try {
                var details = googlePlaceImageWorker.fetchSinglePlaceDetailsAsync(placeId, null).get();
                if (details != null && details.photoUrl() != null) {
                    photoUrl = details.photoUrl();
                    
                    placeSearchResultRepository.updatePhotoUrlByPlaceId(placeId, photoUrl);
                    timeTablePlaceBlockRepository.updatePhotoUrlByPlaceId(placeId, photoUrl);
                }
            } catch (Exception e) {
                log.error("Failed to fetch place details on-demand for placeId: {}", placeId, e);
            }
        }

        // 3. Serve the image resource (only if photoUrl has content)
        if (StringUtils.hasText(photoUrl)) {
            try {
                Resource resource = imageStorage.getImage(photoUrl);
                if (resource != null && resource.exists()) {
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "image/webp")
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                            .body(resource);
                }
            } catch (Exception e) {
                log.error("Error retrieving image resource for photoUrl: {}", photoUrl, e);
            }
        }

        return ResponseEntity.notFound().build();
    }

    private String findPhotoUrlInDb(String placeId) {
        // Try PlaceSearchResult first
        String fromResult = placeSearchResultRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(placeId)
                .map(res -> res.getPhotoUrl())
                // .filter(StringUtils::hasText) // Remove this to allow ""
                .orElse(null);
        
        if (fromResult != null) return fromResult;

        // Then try TimeTablePlaceBlock
        return timeTablePlaceBlockRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(placeId)
                .map(block -> block.getPhotoUrl())
                // .filter(StringUtils::hasText) // Remove this to allow ""
                .orElse(null);
    }
}

