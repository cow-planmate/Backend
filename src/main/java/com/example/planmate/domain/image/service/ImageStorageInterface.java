package com.example.planmate.domain.image.service;

import org.springframework.core.io.Resource;

public interface ImageStorageInterface {
    String uploadImage(String objectName, byte[] data, String contentType);
    Resource getImage(String photoURL);
}
