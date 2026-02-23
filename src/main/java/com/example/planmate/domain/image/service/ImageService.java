package com.example.planmate.domain.image.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

        // If explicitly set to empty string, it means "no photo available" - return 404
        // immediately
        if ("".equals(photoUrl)) {
            return ResponseEntity.notFound().build();
        }

        // 2. If not found in DB (null), try to fetch the reference and return redirect
        if (photoUrl == null) {
            try {
                String photoReference = googlePlaceImageWorker.getPhotoReferenceSync(placeId);
                if (photoReference != null) {
                    // Fetch the original bytes from Google
                    ResponseEntity<byte[]> googleResponse = googlePlaceImageWorker.fetchPhotoBytes(photoReference);
                    byte[] imageBytes = googleResponse.getBody();

                    if (imageBytes != null && imageBytes.length > 0) {
                        // Trigger async processing to save as WebP for future use
                        // We pass quality 6 as it's background processing now
                        googlePlaceImageWorker.processAndSaveImageFromBytesAsync(placeId, imageBytes,
                                (generatedUrl) -> {
                                    placeSearchResultRepository.updatePhotoUrlByPlaceId(placeId, generatedUrl);
                                    timeTablePlaceBlockRepository.updatePhotoUrlByPlaceId(placeId, generatedUrl);
                                });

                        // Proxy the raw bytes back to the user immediately
                        var contentType = googleResponse.getHeaders().getContentType();
                        return ResponseEntity.ok()
                                .contentType(contentType != null ? contentType : MediaType.IMAGE_JPEG)
                                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                                .body(new ByteArrayResource(imageBytes));
                    }
                }

                // If we reach here, no image was found
                placeSearchResultRepository.updatePhotoUrlByPlaceId(placeId, "");
                timeTablePlaceBlockRepository.updatePhotoUrlByPlaceId(placeId, "");
                return ResponseEntity.notFound().build();
            } catch (Exception e) {
                log.error("Failed to fetch/proxy place details for placeId: {}", placeId, e);
                return ResponseEntity.notFound().build();
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

        if (fromResult != null)
            return fromResult;

        // Then try TimeTablePlaceBlock
        return timeTablePlaceBlockRepository.findFirstByPlaceIdAndPhotoUrlIsNotNull(placeId)
                .map(block -> block.getPhotoUrl())
                // .filter(StringUtils::hasText) // Remove this to allow ""
                .orElse(null);
    }
}
