package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = true, length = 120)
    private String email;

    // LOCAL일 때만 사용 가능(소셜로그인이면 null 가능)
    @Column(length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private AuthProvider provider;

    // 소셜 provider user id (LOCAL이면 null 가능)
    @Column(length = 200)
    private String providerUserId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Builder
    public UserEntity(String username, String email, String password, AuthProvider provider, String providerUserId, String name, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.name = name;
        this.role = role;
    }

    public void updateUserInfo(String email, String name) {
        this.email = email;
        this.name = name;
    }
}