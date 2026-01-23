package com.example.planmate.common.externalAPI;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.image.service.ImageStorageInterface;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class GooglePlaceImageWorker {

    public record PlaceExtraDetails(String photoUrl) {}

    @Value("${api.google.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ImageStorageInterface imageStorageService;
    private final java.util.Set<String> processingIds = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    @Async("placeExecutor")
    public CompletableFuture<PlaceExtraDetails> fetchSinglePlaceDetailsAsync(String placeId) {
        if (placeId == null || !processingIds.add(placeId)) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            String finalPhotoReference = null;

            // Google Places API (Legacy) Detail URL
            String detailsUrl = String.format(
                    "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
                    placeId, googleApiKey
            );

            ResponseEntity<Map<String, Object>> detailsResponse = restTemplate.exchange(
                    detailsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = detailsResponse.getBody();
            if (body != null && "OK".equals(body.get("status"))) {
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                if (result != null) {
                    List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
                    if (photos != null && !photos.isEmpty()) {
                        finalPhotoReference = (String) photos.get(0).get("photo_reference");
                    }
                }
            }

            // Now we have the photoReference
            String finalPhotoUrl = ""; // Default to empty string if no photo found
            if (finalPhotoReference != null) {
                String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference=" + finalPhotoReference + "&key=" + googleApiKey;
                ResponseEntity<byte[]> photoBytesResponse = restTemplate.getForEntity(photoUrl, byte[].class);
                byte[] imageBytes = photoBytesResponse.getBody();
                if (imageBytes != null && imageBytes.length > 0) {
                    ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
                    byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));
                    if (webpBytes != null) {
                        String objectName = "googleplace/" + placeId + ".webp";
                        finalPhotoUrl = imageStorageService.uploadImage(objectName, webpBytes, "image/webp");
                    }
                }
            }

            return CompletableFuture.completedFuture(new PlaceExtraDetails(finalPhotoUrl));
        } catch (IOException e) {
            System.err.println("[ASYNC_IO_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ASYNC_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        } finally {
            processingIds.remove(placeId);
        }

        return CompletableFuture.completedFuture(null);
    }
}

