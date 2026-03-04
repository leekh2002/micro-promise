package com.gyuhyuk.micro_promise.fixture.entity;

import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.data.entity.UserRole;

public class UserEntityFixture {
    public static UserEntity create(String username) {
        return UserEntity.builder()
                .username(username)
                .email(username + "@test.com")
                .password("12345")
                .name(username)
                .role(UserRole.ROLE_USER)
                .build();
    }
}
