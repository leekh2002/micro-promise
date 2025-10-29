package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_log",
        indexes = {
                @Index(name = "idx_nlog_project", columnList = "project_id"),
                @Index(name = "idx_nlog_user", columnList = "user_id"),
                @Index(name = "idx_nlog_sent", columnList = "sentAt")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationLogEntity {

    public enum Type { D_MINUS_1, D_DAY, OVERDUE }
    public enum Channel { EMAIL, WEB_PUSH }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Type type;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Channel channel;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean success;
}