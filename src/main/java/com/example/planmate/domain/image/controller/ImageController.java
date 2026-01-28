package com.example.planmate.domain.image.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.planmate.domain.image.service.ImageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Image", description = "이미지 리소스 제공 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/image")
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "장소 이미지 조회", description = "Google Place ID를 사용하여 해당 장소의 이미지를 서버에서 제공합니다.")
    @GetMapping("/place/{placeId}")
    public ResponseEntity<Resource> getGooglePlaceImage(@PathVariable("placeId") String placeId) {
        return imageService.getGooglePlaceImage(placeId);
    }
}