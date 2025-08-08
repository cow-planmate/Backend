package com.example.planmate.entity;

import com.example.planmate.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "plan_editor",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "plan_id"})
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEditor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_editor_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}