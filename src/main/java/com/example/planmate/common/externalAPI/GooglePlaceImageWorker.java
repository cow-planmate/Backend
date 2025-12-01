package com.example.planmate.common.externalAPI;

import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Component
@RequiredArgsConstructor
public class GooglePlaceImageWorker {

    @Value("${api.google.key}")
    private String googleApiKey;

    @Value("${spring.img.url}")
    private String imgUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PlacePhotoRepository placePhotoRepository;

    @SuppressWarnings("unchecked")
    @Async("placeExecutor")
    public CompletableFuture<PlacePhoto> fetchSinglePlaceImageAsync(String placeId) {
        try {
            if(placePhotoRepository.existsById(placeId)) {
                PlacePhoto photo = placePhotoRepository.findById(placeId).get();
                if(photo.getPhotoUrl() != null && !photo.getPhotoUrl().isEmpty()) {
                    return CompletableFuture.completedFuture(placePhotoRepository.findById(placeId).get());
                }
            }
            String fileLocation = imgUrl + placeId + ".webp";
            PlacePhoto photo = PlacePhoto.builder()
                    .placeId(placeId)
                    .photoUrl(fileLocation)
                    .build();
            placePhotoRepository.save(photo);

            String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId + "&fields=photos&key=" + googleApiKey;
            ResponseEntity<Map<String, Object>> detailsResponse = restTemplate.exchange(
                    detailsUrl,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> body = detailsResponse.getBody();
            if (body == null) return CompletableFuture.completedFuture(null);
            Map<String, Object> result = (Map<String, Object>) body.get("result");
            if (result == null) return CompletableFuture.completedFuture(null);
            List<Map<String, Object>> photos = (List<Map<String, Object>>) result.get("photos");
            if (photos == null || photos.isEmpty()) return CompletableFuture.completedFuture(null);
            String photoReference = (String) photos.get(0).get("photo_reference");
            if (photoReference == null) return CompletableFuture.completedFuture(null);

            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + googleApiKey;
            ResponseEntity<byte[]> photoBytesResponse = restTemplate.getForEntity(photoUrl, byte[].class);
            byte[] imageBytes = photoBytesResponse.getBody();
            if (imageBytes == null || imageBytes.length == 0) return CompletableFuture.completedFuture(null);

            ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));
            if (webpBytes == null) return CompletableFuture.completedFuture(null);

            fileLocation = imgUrl + placeId + ".webp";
            File outputFile = new File(fileLocation);
            File parent = outputFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(webpBytes);
            }
            photo = PlacePhoto.builder()
                    .placeId(placeId)
                    .photoUrl(fileLocation)
                    .build();
            placePhotoRepository.save(photo);
            return CompletableFuture.completedFuture(photo);
        } catch (IOException e) {
            System.err.println("[ASYNC_IO_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ASYNC_ERROR] placeId=" + placeId + " msg=" + e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
}
