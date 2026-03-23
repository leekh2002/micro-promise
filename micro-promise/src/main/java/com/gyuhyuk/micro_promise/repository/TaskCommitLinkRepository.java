package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.TaskCommitLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCommitLinkRepository extends JpaRepository<TaskCommitLinkEntity, Long> {
    boolean existsByTaskIdAndCommitSha(Long taskId, String commitSha);
}
