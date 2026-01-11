package com.example.planmate.domain.image.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.planmate.common.externalAPI.GooglePlaceImageWorker;
import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final PlacePhotoRepository placePhotoRepository;
    private final GooglePlaceImageWorker googlePlaceImageWorker;
    private final ImageStorageInterface imageStorage;

    public ResponseEntity<Resource> getGooglePlaceImage(String placeId) {
        if (!StringUtils.hasText(placeId)) {
            return ResponseEntity.badRequest().build();
        }

        PlacePhoto photo = placePhotoRepository.findById(placeId).orElse(null);
        if (photo == null) {
            return ResponseEntity.notFound().build();
        }

        String photoURL = photo.getPhotoUrl();
        if (photoURL == null || photoURL.isBlank()) {
            // Trigger async fetch if not already available
            googlePlaceImageWorker.fetchSinglePlaceImageAsync(placeId);
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = imageStorage.getImage(photoURL);
            if (resource == null || !resource.exists()) {
                // If it's a URL, it might not "exist" in FileSystem sense, but InputStreamResource is valid
                if (!photoURL.startsWith("http")) {
                    return ResponseEntity.notFound().build();
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("image/webp"));

            // ETag 설정은 로컬 파일일 때만 시도 (MinIO는 별도 처리 필요하므로 일단 생략하거나 로그 방지)
            if (!photoURL.startsWith("http")) {
                try {
                    Path photoPath = Paths.get(photoURL);
                    long size = Files.size(photoPath);
                    long mtime = Files.getLastModifiedTime(photoPath).toMillis();
                    headers.setETag("\"" + size + '-' + mtime + "\"");
                } catch (IOException ignore) {}
            }

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
