package com.example.planmate.domain.plan.entity;

import java.util.ArrayList;
import java.util.List;

import com.example.planmate.domain.collaborationRequest.entity.CollaborationRequest;
import com.example.planmate.domain.collaborationRequest.entity.PlanEditor;
import com.example.planmate.domain.framework.annotation.SocketField;
import com.example.planmate.domain.framework.annotation.SocketRoot;
import com.example.planmate.domain.travel.entity.Travel;
import com.example.planmate.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;

@SocketRoot(topic = "plan") // ✅ 최상위 엔터티 (WebSocket 구독 방 기준)
@Entity
@Table(name = "plan", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "plan_name"}))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Plan {

    // -------------------- 기본 키 --------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer planId;

    // -------------------- 주요 필드 --------------------
    @Column(nullable = false)
    private String planName;

    @Column(nullable = false)
    private String departure;

    @Column(nullable = false)
    private int adultCount;

    @Column(nullable = false)
    private int childCount;

    // -------------------- 연관 관계 --------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @SocketField(ignore = true) // ✅ 실시간 동기화 시 제외 (사용자 직접 매핑 대상 아님)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportation_category_id", nullable = false)
    private TransportationCategory transportationCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_id", nullable = false)
    private Travel travel;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @SocketField(ignore = true) // ✅ 하위 엔터티(시간표)는 별도 실시간 동기화 대상
    private List<TimeTable> timeTables = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @SocketField(ignore = true) // ✅ 협업 요청은 실시간 공유 대상 아님
    private List<CollaborationRequest> collaborationRequests = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @SocketField(ignore = true) // ✅ 에디터 권한 리스트는 별도 권한 관리 모듈 처리
    private List<PlanEditor> editors = new ArrayList<>();

    @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @SocketField(ignore = true)
    private PlanShare planShare;

    // -------------------- 비즈니스 로직 --------------------
    public void assignUser(User user) {
        if (user == null) throw new IllegalArgumentException("User는 null일 수 없습니다.");
        this.user = user;
    }

    public void updateCounts(int adult, int child) {
        if (adult < 0 || child < 0)
            throw new IllegalArgumentException("인원 수는 0 이상이어야 합니다.");
        this.adultCount = adult;
        this.childCount = child;
    }

    public void changeTransportationCategory(TransportationCategory transportationCategory) {
        if (transportationCategory == null)
            throw new IllegalArgumentException("TransportationCategory는 null일 수 없습니다.");
        this.transportationCategory = transportationCategory;
    }

    public void changeTravel(Travel travel) {
        if (travel == null)
            throw new IllegalArgumentException("Travel은 null일 수 없습니다.");
        this.travel = travel;
    }

    public void changePlanName(String newName) {
        if (newName == null || newName.isBlank())
            throw new IllegalArgumentException("플랜 이름은 비어 있을 수 없습니다.");
        this.planName = newName;
    }

    public void changeDeparture(String newDeparture) {
        if (newDeparture == null || newDeparture.isBlank())
            throw new IllegalArgumentException("출발지는 비어 있을 수 없습니다.");
        this.departure = newDeparture;
    }

    /**
     * Redis에서 가져온 Plan 데이터로 필드만 업데이트 (연관관계 컬렉션은 건드리지 않음)
     */
    public void updateFromRedis(Plan redisPlan) {
        if (redisPlan == null) return;

        // 기본 필드들만 업데이트
        if (redisPlan.getPlanName() != null) this.planName = redisPlan.getPlanName();
        if (redisPlan.getDeparture() != null) this.departure = redisPlan.getDeparture();
        this.adultCount = redisPlan.getAdultCount();
        this.childCount = redisPlan.getChildCount();

        // 연관관계는 null이 아닐 때만 업데이트
        if (redisPlan.getTransportationCategory() != null)
            this.transportationCategory = redisPlan.getTransportationCategory();
        if (redisPlan.getTravel() != null)
            this.travel = redisPlan.getTravel();
    }
}
