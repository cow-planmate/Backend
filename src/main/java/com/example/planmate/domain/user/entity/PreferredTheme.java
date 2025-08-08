package com.example.planmate.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@Table(name = "preferred_theme")
@Data
@ToString(exclude = "users")
@NoArgsConstructor
@AllArgsConstructor
public class PreferredTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer preferredThemeId;

    @Column(nullable = false)
    private String preferredThemeName;

    @ManyToOne
    @JoinColumn(name = "preferred_theme_category_id", nullable = false)
    private PreferredThemeCategory preferredThemeCategory;

    @ManyToMany(mappedBy = "preferredThemes")
    private Set<User> users;
}