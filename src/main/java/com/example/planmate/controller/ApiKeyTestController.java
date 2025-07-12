package com.example.planmate.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiKeyTestController {

    @Value("${api.google.key}")
    private String googleApiKey;

    @GetMapping("/test-api-key")
    public String testApiKey() {
        return "Google API Key: " + googleApiKey;
    }
}