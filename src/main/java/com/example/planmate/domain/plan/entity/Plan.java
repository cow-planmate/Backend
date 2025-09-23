package com.example.planmate.domain.plan.entity;

import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TimeTable> timeTables = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CollaborationRequest> collaborationRequests = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PlanEditor> editors = new ArrayList<>();

    @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlanShare planShare;

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
