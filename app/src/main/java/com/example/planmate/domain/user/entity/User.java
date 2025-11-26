package com.example.planmate.domain.user.entity;

import com.example.planmate.domain.plan.entity.Plan;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharedsync.shared.presence.annotation.PresenceUser;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@PresenceUser(
        idField = "userId",
        nameField = "nickname"
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private int gender;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @Builder.Default
    private List<Plan> plans = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_preferred_theme",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "preferred_theme_id")
    )
    @JsonIgnore
    @Builder.Default
    private List<PreferredTheme> preferredThemes = new ArrayList<>();

    public void addPlan(Plan plan) {
        this.plans.add(plan);
        plan.assignUser(this);
    }

    public void removePlan(Plan plan) {
        if (plan == null) return;
        this.plans.remove(plan);
        plan.assignUser(null);
    }

    public void addPreferredTheme(PreferredTheme theme) {
        this.preferredThemes.add(theme);
        theme.getUsers().add(this);
    }

    public void removePreferredTheme(PreferredTheme theme) {
        this.preferredThemes.remove(theme);
        theme.getUsers().remove(this);
    }

    public void changePassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어 있을 수 없습니다.");
        }
        this.password = newPassword;
    }

    public void changeNickname(String newNickname) {
        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
        }
        this.nickname = newNickname;
    }

    public void changeAge(int newAge) {
        if (newAge < 0) {
            throw new IllegalArgumentException("나이는 0 이상이어야 합니다.");
        }
        this.age = newAge;
    }

    public void changeGender(int newGender) {
        if (newGender < 0) {
            throw new IllegalArgumentException("성별 값이 올바르지 않습니다.");
        }
        this.gender = newGender;
    }
}
