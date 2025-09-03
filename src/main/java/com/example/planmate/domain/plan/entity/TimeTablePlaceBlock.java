package com.example.planmate.domain.plan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(
        name = "time_table_place_block",
        uniqueConstraints = @UniqueConstraint(columnNames = {"block_start_time", "time_table_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id", nullable = false)
    private PlaceCategory placeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_id", nullable = false)
    private TimeTable timeTable;

    public void changeId(Integer newId) {
        if (newId == null || newId <= 0) {
            throw new IllegalArgumentException("블록 ID는 null이거나 0 이하일 수 없습니다.");
        }
        this.blockId = newId;
    }

    public void changePlaceName(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            throw new IllegalArgumentException("장소 이름은 비어 있을 수 없습니다.");
        }
        this.placeName = placeName;
    }

    public void changePlaceTheme(String placeTheme) {
        if (placeTheme == null || placeTheme.isBlank()) {
            throw new IllegalArgumentException("장소 테마는 비어 있을 수 없습니다.");
        }
        this.placeTheme = placeTheme;
    }

    public void changeRating(float placeRating) {
        if (placeRating < 0) {
            throw new IllegalArgumentException("장소 평점은 0 이상이어야 합니다.");
        }
        this.placeRating = placeRating;
    }

    public void changeLocation(double x, double y) {
        this.xLocation = x;
        this.yLocation = y;
    }

    public void changeAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 비어 있을 수 없습니다.");
        }
        this.placeAddress = address;
    }

    public void changeLink(String link) {
        if (link == null || link.isBlank()) {
            throw new IllegalArgumentException("링크는 비어 있을 수 없습니다.");
        }
        this.placeLink = link;
    }

    public void changeTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }
        this.blockStartTime = start;
        this.blockEndTime = end;
    }

    public void changeCategory(PlaceCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("PlaceCategory는 null일 수 없습니다.");
        }
        this.placeCategory = category;
    }

    public void assignTimeTable(TimeTable timeTable) {
        if (timeTable == null) {
            throw new IllegalArgumentException("TimeTable은 null일 수 없습니다.");
        }
        this.timeTable = timeTable;
    }

    public void updateBlockInfo(
            String placeName,
            String placeTheme,
            float placeRating,
            String placeAddress,
            String placeLink,
            LocalTime blockStartTime,
            LocalTime blockEndTime,
            double xLocation,
            double yLocation,
            PlaceCategory placeCategory
    ) {
        if (blockStartTime != null && blockEndTime != null && blockStartTime.isAfter(blockEndTime)) {
            throw new IllegalArgumentException("블록 시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        this.placeName = placeName;
        this.placeTheme = placeTheme;
        this.placeRating = placeRating;
        this.placeAddress = placeAddress;
        this.placeLink = placeLink;
        this.blockStartTime = blockStartTime;
        this.blockEndTime = blockEndTime;
        this.xLocation = xLocation;
        this.yLocation = yLocation;
        this.placeCategory = placeCategory;
    }

    public void copyFrom(TimeTablePlaceBlock other) {
        if (other == null) {
            throw new IllegalArgumentException("복사할 블록은 null일 수 없습니다.");
        }

        this.updateBlockInfo(
                other.getPlaceName(),
                other.getPlaceTheme(),
                other.getPlaceRating(),
                other.getPlaceAddress(),
                other.getPlaceLink(),
                other.getBlockStartTime(),
                other.getBlockEndTime(),
                other.getXLocation(),
                other.getYLocation(),
                other.getPlaceCategory()
        );

        this.assignTimeTable(other.getTimeTable());
    }
}

