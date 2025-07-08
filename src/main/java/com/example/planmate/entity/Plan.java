package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_name"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    @Column(nullable = false)
    private String planName;

    @Column(nullable = false)
    private String departure;

    @Column(nullable = false)
    private int adultCount;

    @Column(nullable = false)
    private int childCount;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "transportation_category_id", nullable = false)
    private TransportationCategory transportationCategory;

    @ManyToOne
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;
}
