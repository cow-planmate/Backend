    package com.example.planmate.domain.user.entity;
    
    import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
    import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
    import com.example.planmate.domain.plan.entity.Plan;
    import com.fasterxml.jackson.annotation.JsonIgnore;
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
    public class User {
    
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer userId;
    
        @Column(nullable = false)
        private String provider;
    
        @Column
        private String providerId;
    
        @Column(unique = false)
        private String email;
    
        @Column
        private String password;
    
        @Column(nullable = false, unique = true)
        private String nickname;
    
        @Column
        private Integer age;
    
        @Column
        private Integer gender;
    
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
        @JsonIgnore
        @Builder.Default
        private List<Plan> plans = new ArrayList<>();
    
        @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<CollaborationRequest> sentRequests = new ArrayList<>();
    
        @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<CollaborationRequest> receivedRequests = new ArrayList<>();
    
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private List<PlanEditor> planEditors = new ArrayList<>();
    
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
    
        public void changeAge(Integer newAge) {
            this.age = newAge;
        }
    
        public void changeGender(Integer newGender) {
            this.gender = newGender;
        }
    }
