package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "membership",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_membership_team_user", columnNames = {"team_id","user_id"})
        },
        indexes = {
                @Index(name = "idx_membership_team", columnList = "team_id"),
                @Index(name = "idx_membership_user", columnList = "user_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipEntity {   //사용자가 어떤 팀에 속해 있고, 그 안에서 어떤 권한(OWNER / ADMIN / MEMBER)을 가지는지 나타냄

    public enum Role { OWNER, ADMIN, MEMBER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "team_id", nullable = false)
    private TeamEntity team;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16)
    private Role role;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
