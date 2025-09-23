package com.example.planmate.domain.shared.cache;

import org.springframework.stereotype.Component;

import com.example.planmate.domain.plan.repository.TransportationCategoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransportationCategoryCache {
    private final TransportationCategoryRepository transportationCategoryRepository;
}
