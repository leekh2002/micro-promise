package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.ProjectInviteCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectInviteCodeRepository extends JpaRepository<ProjectInviteCodeEntity, Long> {
    Optional<ProjectInviteCodeEntity> findByCode(String code);

    Optional<ProjectInviteCodeEntity> findByProject_Id(Long projectId);
}
