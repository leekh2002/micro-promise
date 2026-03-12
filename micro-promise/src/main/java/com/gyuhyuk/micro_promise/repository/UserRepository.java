package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Boolean existsByUsername(String username);

    UserEntity findByUsername(String username);
}
