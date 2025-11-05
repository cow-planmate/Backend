package com.example.planmate.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "preferred_theme")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PreferredTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer preferredThemeId;

    @Column(nullable = false)
    private String preferredThemeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_theme_category_id", nullable = false)
    private PreferredThemeCategory preferredThemeCategory;

    @ManyToMany(mappedBy = "preferredThemes", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    public void addUser(User user) {
        this.users.add(user);
        user.getPreferredThemes().add(this);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        user.getPreferredThemes().remove(this);
    }

    public void changeName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("선호 테마 이름은 비어 있을 수 없습니다.");
        }
        this.preferredThemeName = newName;
    }

    public void changeCategory(PreferredThemeCategory newCategory) {
        if (newCategory == null) {
            throw new IllegalArgumentException("선호 테마 카테고리는 null일 수 없습니다.");
        }
        this.preferredThemeCategory = newCategory;
    }
}
