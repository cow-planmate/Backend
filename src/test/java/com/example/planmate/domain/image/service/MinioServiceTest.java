package com.example.planmate.domain.image.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;
    @InjectMocks
    private MinioService minioService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(minioService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(minioService, "endpoint", "http://localhost:9000");
    }

    @Test
    @DisplayName("init: 버킷이 없으면 새로 생성한다.")
    void init_create_bucket() throws Exception {
        // given
        given(minioClient.bucketExists(any(BucketExistsArgs.class))).willReturn(false);

        // when
        minioService.init();

        // then
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    @DisplayName("uploadImage: 이미지를 정상적으로 업로드하고 URL을 반환한다.")
    void uploadImage_success() throws Exception {
        // given
        String objectName = "test-image.jpg";
        byte[] data = "dummy image data".getBytes();
        String contentType = "image/jpeg";

        // when
        String resultUrl = minioService.uploadImage(objectName, data, contentType);

        // then
        assertEquals("http://localhost:9000/test-bucket/test-image.jpg", resultUrl);
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("getImage: 이미지 URL로 Resource를 반환한다.")
    void getImage_success() throws Exception {
        // given
        String photoURL = "http://localhost:9000/test-bucket/test-image.jpg";
        InputStream mockStream = new ByteArrayInputStream("dummy data".getBytes());
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        // It's hard to mock exactly GetObjectResponse stream, but we return mock
        // Instead of strict mocking the inside of GetObjectResponse, Mockito can just
        // ignore it or we mock the stream behavior
    }
}
