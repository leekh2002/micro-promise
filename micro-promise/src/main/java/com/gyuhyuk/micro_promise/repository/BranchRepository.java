package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BranchRepository extends JpaRepository<BranchEntity, Long> {
    Optional<BranchEntity> findByRepositoryIdAndBranchName(Long repositoryId, String branchName);
}
