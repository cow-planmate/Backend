package com.example.planmate.domain.plan.repository;

import com.example.planmate.domain.plan.entity.PlacePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlacePhotoRepository extends JpaRepository<PlacePhoto, String> {
}