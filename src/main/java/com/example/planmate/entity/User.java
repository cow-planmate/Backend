package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Table(name = "\"user\"")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    @Setter
    private String username;

    @Setter
    private String password;

    @Setter
    private int gender;

    @Setter

    private int age;

    public User(String email, String username, String password, int gender, int age) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.age = age;
    }
    public User() {

    }
}