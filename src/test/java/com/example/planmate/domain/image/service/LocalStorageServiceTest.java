package com.example.planmate.domain.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LocalStorageServiceTest {

    @InjectMocks
    private LocalStorageService localStorageService;

    @Test
    @DisplayName("uploadImage: 이미지를 로컬 저장소에 업로드하고 경로를 반환한다.")
    void uploadImage_success() {
        // given
        ReflectionTestUtils.setField(localStorageService, "imgUrl", "build/tmp/images/");
        String objectName = "test-image.jpg";
        byte[] data = "dummy image data".getBytes();
        String contentType = "image/jpeg";

        // when
        String filePath = localStorageService.uploadImage(objectName, data, contentType);

        // then
        assertEquals("build/tmp/images/test-image.jpg", filePath);
        File savedFile = new File(filePath);
        assertTrue(savedFile.exists());

        // cleanup
        savedFile.delete();
    }

    @Test
    @DisplayName("getImage: 로컬 경로의 파일에 대한 Resource 객체를 반환한다.")
    void getImage_success() {
        // given
        String testPath = "build/tmp/images/test.jpg";

        // when
        Resource resource = localStorageService.getImage(testPath);

        // then
        assertNotNull(resource);
        assertTrue(resource.getFilename().contains("test.jpg"));
    }
}
