package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "task_branch_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_branch_links_task_branch",
                        columnNames = {"task_id", "branch_id"}
                )
        },
        indexes = {
                @Index(name = "idx_task_branch_links_task", columnList = "task_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TaskBranchLinkEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_branch_links_task"))
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_branch_links_branch"))
    private BranchEntity branch;

    /**
     * 대표/보조 경계가 필요 없으면 null로 두거나,
     * 아예 사용하지 않아도 됨(서비스 정책).
     */
    @Column(name = "is_primary")
    private Boolean primary;

    // 어떤 이유로 연결했는지(선택)
    @Column(length = 500)
    private String note;
}