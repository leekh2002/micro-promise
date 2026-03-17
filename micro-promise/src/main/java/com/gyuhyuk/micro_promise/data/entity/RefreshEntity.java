package com.gyuhyuk.micro_promise.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("refresh_token")
@Getter
@Setter
public class RefreshEntity {

    @Id
    private String refresh;

    private String username;

    private String expiration;

    @TimeToLive
    private Long ttl;
}
