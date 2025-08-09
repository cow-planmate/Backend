package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "time_table_place_block", uniqueConstraints = @UniqueConstraint(columnNames = {"block_start_time", "time_table_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeTablePlaceBlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer blockId;

    @Column(nullable = false)
    private String placeName;

    @Column(nullable = false)
    private String placeTheme;

    @Column(nullable = false)
    private float placeRating;

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

    public void setBlock(TimeTablePlaceBlock block) {
        this.placeName = block.getPlaceName();
        this.placeTheme = block.getPlaceTheme();
        this.placeRating = block.getPlaceRating();
        this.placeAddress = block.getPlaceAddress();
        this.placeLink = block.getPlaceLink();
        this.blockStartTime = block.getBlockStartTime();
        this.blockEndTime = block.getBlockEndTime();
        this.xLocation = block.getXLocation();
        this.yLocation = block.getYLocation();
        this.placeCategory = block.getPlaceCategory();
    }
}
