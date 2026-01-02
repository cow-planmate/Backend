package com.example.planmate.domain.plan.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;
import com.sharedsync.shared.annotation.CacheEntity;
import com.sharedsync.shared.annotation.CacheId;
import com.sharedsync.shared.presence.annotation.PresenceRoot;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@CacheEntity
@PresenceRoot(channel = "plan-presence", idField = "planId")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @CacheId
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
    
    /**
     * Redis에서 가져온 Plan 데이터로 필드만 업데이트 (연관관계 컬렉션은 건드리지 않음)
     */
    public void updateFromRedis(Plan redisPlan) {
        if (redisPlan == null) return;
        
        // 기본 필드들만 업데이트
        if (redisPlan.getPlanName() != null) {
            this.planName = redisPlan.getPlanName();
        }
        if (redisPlan.getDeparture() != null) {
            this.departure = redisPlan.getDeparture();
        }
        this.adultCount = redisPlan.getAdultCount();
        this.childCount = redisPlan.getChildCount();
        
        // 연관관계는 null이 아닐 때만 업데이트
        if (redisPlan.getTransportationCategory() != null) {
            this.transportationCategory = redisPlan.getTransportationCategory();
        }
        if (redisPlan.getTravel() != null) {
            this.travel = redisPlan.getTravel();
        }
        // timeTables, collaborationRequests, editors는 의도적으로 업데이트하지 않음
    }
}
