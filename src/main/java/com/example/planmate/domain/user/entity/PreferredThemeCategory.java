package com.example.planmate.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "preferred_theme_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferredThemeCategory {
    @Id
    private Integer preferredThemeCategoryId;

    @Column(nullable = false)
    private String preferredThemeCategoryName;
}
