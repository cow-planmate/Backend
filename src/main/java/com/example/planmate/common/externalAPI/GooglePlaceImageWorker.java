package com.example.planmate.common.externalAPI;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.example.planmate.domain.image.service.ImageStorageInterface;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class GooglePlaceImageWorker {

    @Value("${api.google.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PlacePhotoRepository placePhotoRepository;
    private final ImageStorageInterface imageStorageService;
    private final java.util.Set<String> processingIds = ConcurrentHashMap.newKeySet();

    @SuppressWarnings("unchecked")
    @Async("placeExecutor")
    public CompletableFuture<PlacePhoto> fetchSinglePlaceImageAsync(String placeId) {
        if (placeId == null || !processingIds.add(placeId)) {
            return CompletableFuture.completedFuture(null);
        }
        try {
            Optional<PlacePhoto> existing = placePhotoRepository.findById(placeId);
            if (existing.isPresent() && existing.get().getPhotoUrl() != null && !existing.get().getPhotoUrl().isEmpty()) {
                return CompletableFuture.completedFuture(existing.get());
            }
            
            String detailsUrl = "https://places.googleapis.com/v1/places/" + placeId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", googleApiKey);
            headers.set("X-Goog-FieldMask", "photos");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> detailsResponse = restTemplate.exchange(
                    detailsUrl,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = detailsResponse.getBody();
            if (body == null) return CompletableFuture.completedFuture(null);
            
            List<Map<String, Object>> photos = (List<Map<String, Object>>) body.get("photos");
            if (photos == null || photos.isEmpty()) return CompletableFuture.completedFuture(null);
            String photoName = (String) photos.get(0).get("name");
            if (photoName == null) return CompletableFuture.completedFuture(null);

            String photoUrl = "https://places.googleapis.com/v1/" + photoName + "/media?maxHeightPx=400&maxWidthPx=400&key=" + googleApiKey;
            ResponseEntity<byte[]> photoBytesResponse = restTemplate.getForEntity(photoUrl, byte[].class);
            byte[] imageBytes = photoBytesResponse.getBody();
            if (imageBytes == null || imageBytes.length == 0) return CompletableFuture.completedFuture(null);

            ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));
            if (webpBytes == null) return CompletableFuture.completedFuture(null);

            String objectName = "googlePlace/" + placeId + ".webp";
            String photoUrlResult = imageStorageService.uploadImage(objectName, webpBytes, "image/webp");

            PlacePhoto photo = PlacePhoto.builder()
                    .placeId(placeId)
                    .photoUrl(photoUrlResult)
                    .build();
            try {
                photo = placePhotoRepository.save(photo);
            } catch (DataIntegrityViolationException e) {
                photo = placePhotoRepository.findById(placeId).orElse(photo);
            }
            return CompletableFuture.completedFuture(photo);
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
