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
public class TaskDoneRequestEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 완료 요청이 발생한 task
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_done_requests_task"))
    private TaskEntity task;

    // 요청을 보낸 멤버(해당 task의 TaskOwner 또는 공석일 때 합의된 멤버)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_done_requests_requester"))
    private ProjectMemberEntity requester;

    // 실제로 수락/거절 권한을 가진 대상 직계 부모 TaskOwner 또는 가장 가까운 조상 TaskOwner
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

    @Builder
    private TaskDoneRequestEntity(TaskEntity task, ProjectMemberEntity requester, ProjectMemberEntity targetTaskOwner,
                                  DoneRequestStatus status, String message, LocalDateTime decidedAt) {
        this.task = task;
        this.requester = requester;
        this.targetTaskOwner = targetTaskOwner;
        this.status = status;
        this.message = message;
        this.decidedAt = decidedAt;
    }

    // 완료 요청을 승인 상태로 변경한다.
    public void accept() {
        // 요청 상태를 ACCEPTED로 기록한다.
        this.status = DoneRequestStatus.ACCEPTED;
        // 승인 시각을 현재 시각으로 기록한다.
        this.decidedAt = LocalDateTime.now();
    }

    // 완료 요청을 거절 상태로 변경한다.
    public void reject() {
        // 요청 상태를 REJECTED로 기록한다.
        this.status = DoneRequestStatus.REJECTED;
        // 거절 시각을 현재 시각으로 기록한다.
        this.decidedAt = LocalDateTime.now();
    }

    // 완료 요청을 취소 상태로 변경한다.
    public void cancel() {
        // 요청 상태를 CANCELED로 기록한다.
        this.status = DoneRequestStatus.CANCELED;
        // 취소 시각을 현재 시각으로 기록한다.
        this.decidedAt = LocalDateTime.now();
    }
}
