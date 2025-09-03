package com.example.planmate.domain.travel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "travel_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TravelCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer travelCategoryId;

    @Column(nullable = false, unique = true)
    private String travelCategoryName;

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("여행 카테고리 이름은 비어 있을 수 없습니다.");
        }
        this.travelCategoryName = newName;
    }
}
