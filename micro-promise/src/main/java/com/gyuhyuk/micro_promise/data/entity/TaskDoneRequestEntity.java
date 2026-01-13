package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "task_done_requests",
        indexes = {
                @Index(name = "idx_done_requests_task", columnList = "task_id"),
                @Index(name = "idx_done_requests_target", columnList = "target_task_owner_member_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TaskDoneRequestEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 완료 요청이 발생한 task
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_done_requests_task"))
    private TaskEntity task;

    // 요청을 보낸 사람(해당 task의 TaskOwner 또는 공석일 때 임의 멤버)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_done_requests_requester"))
    private ProjectMemberEntity requester;

    // 실제로 수락/거절 권한을 가진 대상(직계부모 TaskOwner or 가장 가까운 조상 TaskOwner)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_task_owner_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_done_requests_target"))
    private ProjectMemberEntity targetTaskOwner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DoneRequestStatus status;

    @Column(length = 1000)
    private String message;

    private LocalDateTime decidedAt;
}