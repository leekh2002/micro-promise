package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.TaskBranchLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskBranchLinkRepository extends JpaRepository<TaskBranchLinkEntity, Long> {
    boolean existsByTaskIdAndBranchId(Long taskId, Long branchId);
}
