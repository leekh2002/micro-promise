package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByProjectId(Long projectId);

}
