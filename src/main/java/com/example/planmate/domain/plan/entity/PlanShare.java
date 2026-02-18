package com.example.planmate.domain.plan.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_share")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanShare {

    @Id
    private UUID planId;  // PK이자 FK

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(name = "share_token", nullable = false, unique = true, length = 128)
    private String shareToken;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public PlanShare(Plan plan, String shareToken, Boolean isActive, LocalDateTime createdAt) {
        this.plan = plan;
        this.shareToken = shareToken;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // 공유 활성화/비활성화 메서드
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // 토큰 변경 메서드
//    public void changeShareToken(String shareToken) {
//        if (shareToken == null || shareToken.isBlank()) {
//            throw new IllegalArgumentException("shareToken은 비어 있을 수 없습니다.");
//        }
//        this.shareToken = shareToken;
//    }
}

