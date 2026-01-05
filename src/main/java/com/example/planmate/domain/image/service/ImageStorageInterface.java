package com.example.planmate.domain.image.service;

public interface ImageStorageInterface {
    String uploadImage(String objectName, byte[] data, String contentType);
}
