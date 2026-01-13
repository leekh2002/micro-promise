package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "task_commit_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_commit_links_task_commit",
                        columnNames = {"task_id", "commit_sha"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TaskCommitLinkEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_commit_links_task"))
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "commit_sha", referencedColumnName = "sha", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_commit_links_commit"))
    private CommitEntity commit;

    /**
     * "팀원이 커밋을 확인하고 task 완료 여부를 결정" 요구사항 반영:
     * 커밋을 Task에 '근거로 채택'한 사람.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linked_by_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_commit_links_linker"))
    private ProjectMemberEntity linkedBy;

    @Column(nullable = false)
    private boolean accepted; // 이 커밋을 완료 근거로 인정했는지

    @Column(length = 500)
    private String comment;
}