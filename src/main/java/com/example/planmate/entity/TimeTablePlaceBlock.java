package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "time_table_place_block", uniqueConstraints = @UniqueConstraint(columnNames = {"block_start_time", "time_table_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeTablePlaceBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer blockId;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private String placeTheme;

    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal placeRating;

    @Column(nullable = false)
    private String placeAddress;

    @Column(nullable = false)
    private String placeLink;

    @Column(nullable = false)
    private LocalTime blockStartTime;

    @Column(nullable = false)
    private LocalTime blockEndTime;

    @Column(nullable = false)
    private double xLocation;

    @Column(nullable = false)
    private double yLocation;

    @ManyToOne
    @JoinColumn(name = "place_category_id", nullable = false)
    private PlaceCategory placeCategory;

    @ManyToOne
    @JoinColumn(name = "time_table_id", nullable = false)
    private TimeTable timeTable;
}
