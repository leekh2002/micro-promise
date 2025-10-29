package com.gyuhyuk.micro_promise.data.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "project",
        indexes = {
                @Index(name = "idx_project_parent", columnList = "parent_id"),
                @Index(name = "idx_project_team", columnList = "team_id"),
                @Index(name = "idx_project_user", columnList = "user_id"),
                @Index(name = "idx_project_creator", columnList = "creator_id"),
                @Index(name = "idx_project_due", columnList = "dueAt")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProjectEntity {

    public enum Status { PLANNED, IN_PROGRESS, DONE, CANCELED }
    public enum Priority { LOW, MEDIUM, HIGH }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 계층 구조(자기 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ProjectEntity parent;

    // 소유자: 개인 또는 팀 (둘 중 하나만 값이 존재)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id")
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private UserEntity user;

    // 생성자(기록용)
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Status status;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Priority priority;

    private LocalDateTime startAt;
    private LocalDateTime dueAt;

    /**
     * 진행률(0.0~100.0) — 리프: 체크리스트/담당자 기반 계산,
     * 비리프: 하위 프로젝트 평균 등 규칙으로 갱신
     */
    private Double progress;

    // 트리 깊이(루트=0)
    @Column(nullable = false)
    private Integer depth;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}