package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PlaceCategory {

    @Id
    private Integer placeCategoryId;

    @Column(nullable = false, unique = true)
    private String placeCategoryName;

    public void changeCategoryName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("카테고리 이름은 비어 있을 수 없습니다.");
        }
        this.placeCategoryName = newName;
    }
}
