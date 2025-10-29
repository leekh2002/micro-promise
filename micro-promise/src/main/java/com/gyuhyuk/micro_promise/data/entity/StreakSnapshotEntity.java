package com.gyuhyuk.micro_promise.data.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "streak_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_streak_user_date", columnNames = {"user_id","date"})
        },
        indexes = {
                @Index(name = "idx_streak_user", columnList = "user_id"),
                @Index(name = "idx_streak_date", columnList = "date")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StreakSnapshotEntity { //리포트(이행률 통계 표시)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int dailyDoneCount;

    @CreationTimestamp
    private LocalDateTime createdAt;
}