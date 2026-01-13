package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "git_commits",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_git_commits_sha", columnNames = {"sha"})
        },
        indexes = {
                @Index(name = "idx_git_commits_branch", columnList = "branch_id"),
                @Index(name = "idx_git_commits_committed_at", columnList = "committed_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommitEntity extends BaseTimeEntity {

    @Id
    @Column(length = 64)
    private String sha; // PK를 sha로

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_commits_branch"))
    private BranchEntity branch;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 200)
    private String authorName;

    @Column(name = "committed_at", nullable = false)
    private LocalDateTime committedAt;
}