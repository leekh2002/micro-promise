package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignee",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assignee_project_user", columnNames = {"project_id","user_id"})
        },
        indexes = {
                @Index(name = "idx_assignee_project", columnList = "project_id"),
                @Index(name = "idx_assignee_user", columnList = "user_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssigneeEntity {

    public enum Role { LEADER, CONTRIBUTOR }
    public enum Status { PENDING, DONE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Role role;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Status status;

    @CreationTimestamp
    private LocalDateTime assignedAt;

    private LocalDateTime completedAt;
}