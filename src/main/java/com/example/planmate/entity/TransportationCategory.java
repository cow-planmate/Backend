package com.example.planmate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transportation_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportationCategory {
    @Id
    private Integer transportationCategoryId;

    @Column(nullable = false)
    private String transportationCategoryName;
}
