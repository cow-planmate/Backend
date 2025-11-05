package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transportation_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TransportationCategory {

    @Id
    private Integer transportationCategoryId;

    @Column(nullable = false, unique = true)
    private String transportationCategoryName;

    public TransportationCategory(Integer transportationCategoryId) {
        this.transportationCategoryId = transportationCategoryId;
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("교통수단 카테고리 이름은 비어 있을 수 없습니다.");
        }
        this.transportationCategoryName = newName;
    }
}
