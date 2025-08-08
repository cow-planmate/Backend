package com.example.planmate.domain.travel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Travel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer travelId;

    @Column(nullable = false)
    private String travelName;

    @ManyToOne
    @JoinColumn(name = "travel_category_id", nullable = false)
    private TravelCategory travelCategory;

    public Travel(Integer travelId, String travelName, Integer travelCategoryId, String travelCategoryName) {
        this.travelId = travelId;
        this.travelName = travelName;
        this.travelCategory = new TravelCategory(travelCategoryId, travelCategoryName);
    }
}