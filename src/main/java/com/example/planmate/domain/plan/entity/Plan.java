package com.example.planmate.domain.plan.entity;

import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

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
@Table(name = "plan", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportation_category_id", nullable = false)
    private TransportationCategory transportationCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;

    public void assignUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User는 null일 수 없습니다.");
        }
        this.user = user;
    }

    public void updateCounts(int adult, int child) {
        if (adult < 0 || child < 0) {
            throw new IllegalArgumentException("인원 수는 0 이상이어야 합니다.");
        }
        this.adultCount = adult;
        this.childCount = child;
    }

    public void changeTransportationCategory(TransportationCategory transportationCategory) {
        if (transportationCategory == null) throw new IllegalArgumentException("TransportationCategory는 null일 수 없습니다.");
        this.transportationCategory = transportationCategory;
    }

    public void changeTravel(Travel travel) {
        if (travel == null) throw new IllegalArgumentException("Travel은 null일 수 없습니다.");
        this.travel = travel;
    }

    public void changePlanName(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("플랜 이름은 비어 있을 수 없습니다.");
        }
        this.planName = newName;
    }

    public void changeDeparture(String newDeparture) {
        if (newDeparture == null || newDeparture.isBlank()) {
            throw new IllegalArgumentException("출발지는 비어 있을 수 없습니다.");
        }
        this.departure = newDeparture;
    }
}
