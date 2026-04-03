package com.example.planmate.common.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-path}")
    private Resource serviceAccountResource;

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        InputStream serviceAccount = null;

        // 1. JSON 문자열 설정이 있는지 먼저 확인
        if (StringUtils.hasText(serviceAccountJson)) {
            log.info("Firebase 설정을 속성(JSON)으로부터 직접 로드합니다.");
            serviceAccount = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
        } 
        // 2. 설정이 없다면 기존 리소스 파일 확인
        else if (serviceAccountResource != null && serviceAccountResource.exists()) {
            log.info("Firebase 설정을 리소스 파일({})로부터 로드합니다.", serviceAccountResource.getFilename());
            serviceAccount = serviceAccountResource.getInputStream();
        }

        if (serviceAccount == null) {
            log.warn("Firebase 설정 정보(속성 또는 파일)를 찾을 수 없습니다. Firebase 기능이 비활성화됩니다.");
            return null;
        }

        try (InputStream is = serviceAccount) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Firebase 초기화 중 오류 발생: {}", e.getMessage());
            return null;
        }
    }
}
