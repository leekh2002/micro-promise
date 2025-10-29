package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invite",
        indexes = {
                @Index(name = "idx_invite_team", columnList = "team_id"),
                @Index(name = "idx_invite_code", columnList = "code", unique = true)
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InviteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, unique = true, length = 64)
    private String code;          // UUID 등

    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
}