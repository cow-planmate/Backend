package com.example.planmate.domain.plan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCategory {
    @Id
    private Integer placeCategoryId;

    @Column(nullable = false)
    private String placeCategoryName;
}