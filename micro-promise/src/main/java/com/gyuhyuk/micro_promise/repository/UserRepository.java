package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Boolean existsByUsername(String username);

    UserEntity findByUsername(String username);
}
