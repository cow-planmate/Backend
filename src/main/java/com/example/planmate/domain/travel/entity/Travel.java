package com.example.planmate.domain.travel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Travel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer travelId;

    @Column(nullable = false)
    private String travelName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_category_id", nullable = false)
    private TravelCategory travelCategory;

    @Column
    private Double latitude;

    @Column
    private Double longitude;


    public Travel(Integer travelId, String travelName, Integer travelCategoryId, String travelCategoryName) {
        this.travelId = travelId;
        this.travelName = travelName;
        this.travelCategory = new TravelCategory(travelCategoryId, travelCategoryName);
    }

    // ------------------------
    // Business Logic
    // ------------------------

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("여행 이름은 비어 있을 수 없습니다.");
        }
        this.travelName = newName;
    }

    public void changeCategory(TravelCategory newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("여행 카테고리는 null일 수 없습니다.");
        }
        this.travelCategory = newCategory;
    }

    public boolean hasCoordinate() {
        return latitude != null && longitude != null;
    }

    public void initializeCoordinate(double lat, double lng) {
        if (hasCoordinate()) {
            return; // 이미 세팅되어 있으면 무시
        }
        this.latitude = lat;
        this.longitude = lng;
    }
}