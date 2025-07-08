package com.gyuhyuk.price_alert.repository;

import com.gyuhyuk.price_alert.data.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
