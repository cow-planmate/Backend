package com.example.planmate.domain.image.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.common.externalAPI.GooglePlaceImageWorker;
import com.example.planmate.domain.image.entity.PlacePhoto;
import com.example.planmate.domain.image.repository.PlacePhotoRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {
    private final PlacePhotoRepository placePhotoRepository;
    private final GooglePlaceImageWorker googlePlaceImageWorker;

    @Value("${spring.img.url}")
    private String baseDir;

    @GetMapping("/place/{path}")
    public ResponseEntity<Resource> getGooglePlaceImage(@PathVariable("path") String placeId) {
        if (!StringUtils.hasText(placeId)) {
            return ResponseEntity.badRequest().build();
        }
        PlacePhoto photo = placePhotoRepository.findById(placeId).orElse(null);
        if (photo == null) {
            return ResponseEntity.notFound().build();
        }
        String photoURL = photo.getPhotoURL();
        if(photoURL == null || photoURL.isBlank()) {
            googlePlaceImageWorker.fetchSinglePlaceImageAsync(placeId);
        }
        try {
            FileSystemResource resource = new FileSystemResource(photoURL);
            Path photoPath = Paths.get(photoURL);
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic());
            
            try {
                long size = Files.size(photoPath);
                long mtime = Files.getLastModifiedTime(photoPath).toMillis();
                headers.setETag("\"" + size + '-' + mtime + "\"");
            } catch (IOException ignore) {
                System.out.println("Failed to set ETag for image: " + photoURL);
            }
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (InvalidPathException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}