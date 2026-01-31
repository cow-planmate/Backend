package com.example.planmate.domain.plan.entity;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sharedsync.shared.annotation.CacheEntity;
import com.sharedsync.shared.annotation.CacheId;
import com.sharedsync.shared.annotation.ParentId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "time_table_place_block",
        uniqueConstraints = @UniqueConstraint(columnNames = {"block_start_time", "time_table_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@CacheEntity
public class TimeTablePlaceBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @CacheId
    private Integer blockId;

    @Column(name = "place_id", length = 100)
    private String placeId;

    @Column(nullable = false)
    private String placeName;

    @Column
    private String placeTheme;

    @Column
    private Float placeRating;

    @Column
    private String placeAddress;

    @Column
    private String placeLink;

    @Column(columnDefinition = "TEXT")
    private String photoUrl;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Column(nullable = false)
    private LocalTime blockStartTime;

    @Column(nullable = false)
    private LocalTime blockEndTime;

    @Column
    private Double xLocation;

    @Column
    private Double yLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_category_id", nullable = false)
    private PlaceCategory placeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_id", nullable = false)
    @ParentId(TimeTable.class)
    private TimeTable timeTable;

    public void changeId(Integer newId) {
        this.blockId = newId;
    }

    public void changePlaceName(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            throw new IllegalArgumentException("장소 이름은 비어 있을 수 없습니다.");
        }
        this.placeName = placeName;
    }

    public void changePlaceTheme(String placeTheme) {
        this.placeTheme = placeTheme;
    }

    public void changeRating(Float placeRating) {
        this.placeRating = placeRating;
    }

    public void changeLocation(Double x, Double y) {
        this.xLocation = x;
        this.yLocation = y;
    }

    public void changeAddress(String address) {
        this.placeAddress = address;
    }

    public void changeLink(String link) {
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
            String placeId,
            String placeName,
            String placeTheme,
            Float placeRating,
            String placeAddress,
            String placeLink,
            String photoUrl,
            String memo,
            LocalTime blockStartTime,
            LocalTime blockEndTime,
            Double xLocation,
            Double yLocation,
            PlaceCategory placeCategory
    ) {
        if (blockStartTime != null && blockEndTime != null && blockStartTime.isAfter(blockEndTime)) {
            throw new IllegalArgumentException("블록 시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        this.placeId = placeId;
        this.placeName = placeName;
        this.placeTheme = placeTheme;
        this.placeRating = placeRating;
        this.placeAddress = placeAddress;
        this.placeLink = placeLink;
        this.photoUrl = photoUrl;
        this.memo = memo;
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
                other.getPlaceId(),
                other.getPlaceName(),
                other.getPlaceTheme(),
                other.getPlaceRating(),
                other.getPlaceAddress(),
                other.getPlaceLink(),
                other.getPhotoUrl(),
                other.getMemo(),
                other.getBlockStartTime(),
                other.getBlockEndTime(),
                other.getXLocation(),
                other.getYLocation(),
                other.getPlaceCategory()
        );

        this.assignTimeTable(other.getTimeTable());
    }
}

