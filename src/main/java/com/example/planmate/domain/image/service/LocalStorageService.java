package com.example.planmate.domain.image.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements ImageStorageInterface {

    @Value("${spring.img.url:images/googleplace/}")
    private String imgUrl;

    @Override
    public String uploadImage(String objectName, byte[] data, String contentType) {
        try {
            String fileLocation = imgUrl + objectName;
            File outputFile = new File(fileLocation);
            File parent = outputFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(data);
            }
            return fileLocation;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image locally", e);
        }
    }
}
