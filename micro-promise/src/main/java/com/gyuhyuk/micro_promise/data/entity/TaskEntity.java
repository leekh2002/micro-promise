package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_project", columnList = "project_id"),
                @Index(name = "idx_tasks_parent", columnList = "parent_task_id"),
                @Index(name = "idx_tasks_status", columnList = "status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TaskEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_tasks_project"))
    private ProjectEntity project;

    // 계층형 구조 (부모)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id",
            foreignKey = @ForeignKey(name = "fk_tasks_parent"))
    private TaskEntity parent;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 5000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TaskStatus status;

    // 진행률(0~100) - “자식 기준 계산”은 서비스 레이어에서 계산해도 되고, 캐시로 유지해도 됨
    @Column(nullable = false)
    private int progress;

    // 같은 레벨에서 순서 부여
    @Column(nullable = false)
    private int orderIndex;

    /**
     * TaskOwner (공석 가능)
     * - 요구사항: 루트 TaskOwner = Owner
     * - 공석이면 "가장 가까운 조상 TaskOwner" 규칙은 쿼리/서비스 로직에서 해결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_owner_member_id",
            foreignKey = @ForeignKey(name = "fk_tasks_task_owner"))
    private ProjectMemberEntity taskOwner;
}