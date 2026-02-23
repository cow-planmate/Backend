package com.example.planmate.common.externalAPI;

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

    public record PlaceExtraDetails(String photoUrl) {
    }

    @Value("${api.google.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ImageStorageInterface imageStorageService;
    private final java.util.Set<String> processingIds = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    public String getPhotoReferenceSync(String placeId) {
        try {
            String detailsUrl = String.format(
                    "https://maps.googleapis.com/maps/api/place/details/json?place_id=%s&fields=photos&key=%s",
                    placeId, googleApiKey);

            ResponseEntity<Map<String, Object>> detailsResponse = restTemplate.exchange(
                    detailsUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = detailsResponse.getBody();
            if (body != null && "OK".equals(body.get("status"))) {
                Map<String, Object> result = (Map<String, Object>) body.get("result");
                if (result != null) {
                    List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
                    if (photos != null && !photos.isEmpty()) {
                        return (String) photos.get(0).get("photo_reference");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SYNC_ERROR] placeId=" + placeId + " detail API msg=" + e.getMessage());
        }
        return null; // missing or error
    }

    public String buildGooglePhotoUrl(String photoReference) {
        if (photoReference == null)
            return null;
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference="
                + photoReference + "&key=" + googleApiKey;
    }

    public ResponseEntity<byte[]> fetchPhotoBytes(String photoReference) {
        String photoUrl = buildGooglePhotoUrl(photoReference);
        return restTemplate.getForEntity(photoUrl, byte[].class);
    }

    @Async("customPlaceExecutor")
    public void processAndSaveImageFromBytesAsync(String placeId, byte[] imageBytes,
            java.util.function.Consumer<String> onSuccess) {
        if (placeId == null || imageBytes == null || imageBytes.length == 0 || !processingIds.add(placeId)) {
            return;
        }
        try {
            ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            // Use quality 6 for better compression as it's now back in the background
            byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));
            if (webpBytes != null) {
                String objectName = "googleplace/" + placeId + ".webp";
                String photoUrl = imageStorageService.uploadImage(objectName, webpBytes, "image/webp");
                if (photoUrl != null && onSuccess != null) {
                    onSuccess.accept(photoUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("[ASYNC_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        } finally {
            processingIds.remove(placeId);
        }
    }

    @SuppressWarnings("unchecked")
    @Async("customPlaceExecutor")
    public CompletableFuture<PlaceExtraDetails> fetchSinglePlaceDetailsAsync(String placeId, String photoReference) {
        if (placeId == null || !processingIds.add(placeId)) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            String finalPhotoReference = photoReference;
            if (finalPhotoReference == null) {
                finalPhotoReference = getPhotoReferenceSync(placeId);
            }

            if (finalPhotoReference != null) {
                ResponseEntity<byte[]> response = fetchPhotoBytes(finalPhotoReference);
                byte[] imageBytes = response.getBody();
                if (imageBytes != null && imageBytes.length > 0) {
                    ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
                    byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));
                    if (webpBytes != null) {
                        String objectName = "googleplace/" + placeId + ".webp";
                        String finalPhotoUrl = imageStorageService.uploadImage(objectName, webpBytes, "image/webp");
                        return CompletableFuture.completedFuture(new PlaceExtraDetails(finalPhotoUrl));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ASYNC_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        } finally {
            processingIds.remove(placeId);
        }
        return CompletableFuture.completedFuture(null);
    }

}
