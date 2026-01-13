package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "project_invite_codes",
        indexes = {
                @Index(name = "idx_invite_project", columnList = "project_id"),
                @Index(name = "idx_invite_expires", columnList = "expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invite_code", columnNames = {"code"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectInviteCodeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invite_project"))
    private ProjectEntity project;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked;
}