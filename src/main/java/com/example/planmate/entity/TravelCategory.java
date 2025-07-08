package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer travelCategoryId;

    @Column(nullable = false)
    private String travelCategoryName;
}