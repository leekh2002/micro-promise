package com.gyuhyuk.price_alert.repository;

import com.gyuhyuk.price_alert.data.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    //@EntityGraph은 쿼리가 수행이 될때 Lazy조회가 아니고 Eager조회로 authorities정보를 같이 가져오게됨
    @EntityGraph(attributePaths = "authorities")
    Optional<UserEntity> findOneWithAuthoritiesByUsername(String username);
}
