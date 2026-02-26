package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitRepoRepository extends JpaRepository<ProjectRepositoryEntity, Long> {
}
