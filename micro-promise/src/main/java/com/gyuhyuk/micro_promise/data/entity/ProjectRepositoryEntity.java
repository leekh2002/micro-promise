package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "project_repositories",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_repo_project", columnNames = {"project_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectRepositoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_repo_project"))
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GitProvider provider;

    @Column(nullable = false, length = 200)
    private String repoOwner; // org/user

    @Column(nullable = false, length = 200)
    private String repoName;

    @Column(nullable = false, length = 1000)
    private String repoUrl;
}