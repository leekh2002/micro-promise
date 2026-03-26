package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.TaskBranchLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskBranchLinkRepository extends JpaRepository<TaskBranchLinkEntity, Long> {
    boolean existsByTaskIdAndBranchId(Long taskId, Long branchId);
    // push webhook 처리 시 "이 branch에 연결된 task들"을 한 번에 가져오기 위해 사용한다.
    List<TaskBranchLinkEntity> findByBranchId(Long branchId);
}
