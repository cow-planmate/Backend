package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"preferredThemes", "plans"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Plan> plans;

    @ManyToMany
    @JoinTable(
            name = "user_preferred_theme",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "preferred_theme_id")
    )
    private List<PreferredTheme> preferredThemes = new ArrayList<>();

}