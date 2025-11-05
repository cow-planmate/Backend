package com.example.planmate.domain.image.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.planmate.domain.image.entity.PlacePhoto;

public interface PlacePhotoRepository extends JpaRepository<PlacePhoto, String>{

}
