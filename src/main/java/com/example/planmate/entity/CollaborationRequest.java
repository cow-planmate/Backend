package com.example.planmate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaboration_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CollaborationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_request_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_request_type", length = 10, nullable = false)
    private CollaborationRequestType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "collaboration_request_status", length = 10, nullable = false)
    private CollaborationRequestStatus status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public void changeStatus(CollaborationRequestStatus newStatus) {
        if (this.status != CollaborationRequestStatus.PENDING) {
            throw new IllegalStateException("요청 상태는 PENDING에서만 변경 가능합니다.");
        }

        this.status = newStatus;
    }
}
