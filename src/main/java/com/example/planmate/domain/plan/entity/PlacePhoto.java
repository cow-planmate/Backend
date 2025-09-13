package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_photo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlacePhoto {

    @Id
    @Column(length = 100)
    private String placeId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String photoUrl;

//    // 필요하면 URL 변경 메서드 추가
//    public void changePhotoUrl(String newUrl) {
//        if (newUrl == null || newUrl.isBlank()) {
//            throw new IllegalArgumentException("사진 URL은 비어 있을 수 없습니다.");
//        }
//        this.photoUrl = newUrl;
//    }
}
