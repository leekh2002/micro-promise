package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "task_assignees",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_assignees_task_member",
                        columnNames = {"task_id", "project_member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_task_assignees_task", columnList = "task_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TaskAssigneeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_assignees_task"))
    private TaskEntity task;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_assignees_project_member"))
    private ProjectMemberEntity projectMember;
}