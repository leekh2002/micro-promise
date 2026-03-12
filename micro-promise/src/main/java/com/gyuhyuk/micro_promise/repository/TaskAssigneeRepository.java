package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.TaskAssigneeEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAssigneeRepository extends JpaRepository<TaskAssigneeEntity, Long> {

    @Query("""
        select ta.task
        from TaskAssigneeEntity ta
        where ta.projectMember.user.username = :username
    """)
    List<TaskEntity> findTaskByAssigneeUsername(@Param("username")String username);

    TaskRole findRoleByTaskIdAndProjectMemberUserUsername(Long taskId, String username);
}
