package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.RefreshEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshRepository extends JpaRepository<RefreshEntity, Long> {
    Boolean existsByRefresh(String refresh);

    // db를 변경하는 delete이므로 @Transactional 필요
    @Transactional
    void deleteByRefresh(String refresh);
}
