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
public class ProjectRepositoryEntity extends BaseTimeEntity {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_repo_project"))
    private ProjectEntity project;

    @Column(nullable = false, length = 200)
    private String repoName;

    @Column(nullable = false, length = 1000)
    private String repoUrl;

    @Builder
    public ProjectRepositoryEntity(Long id, ProjectEntity project, String repoName, String repoUrl) {
        this.id = id;
        this.project = project;
        this.repoName = repoName;
        this.repoUrl = repoUrl;
    }
}