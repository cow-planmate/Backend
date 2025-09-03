package com.example.planmate.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "preferred_theme_category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PreferredThemeCategory {

    @Id
    private Integer preferredThemeCategoryId;

    @Column(nullable = false, unique = true)
    private String preferredThemeCategoryName;

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("선호 테마 카테고리 이름은 비어 있을 수 없습니다.");
        }
        this.preferredThemeCategoryName = newName;
    }
}
