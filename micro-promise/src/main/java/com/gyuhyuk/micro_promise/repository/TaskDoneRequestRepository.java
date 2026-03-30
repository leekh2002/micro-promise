package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.DoneRequestStatus;
import com.gyuhyuk.micro_promise.data.entity.TaskDoneRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

// 완료 요청 저장/조회용 JPA repository다.
public interface TaskDoneRequestRepository extends JpaRepository<TaskDoneRequestEntity, Long> {
    // 특정 task에 대기 중인 완료 요청이 이미 있는지 확인한다.
    boolean existsByTaskIdAndStatus(Long taskId, DoneRequestStatus status);
}
