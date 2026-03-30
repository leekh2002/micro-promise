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

    // 특정 task에서 특정 username이 가진 역할을 조회한다.
    @Query("""
        select ta.role
        from TaskAssigneeEntity ta
        where ta.task.id = :taskId
          and ta.projectMember.user.username = :username
    """)
    TaskRole findRoleByTaskIdAndProjectMemberUserUsername(@Param("taskId") Long taskId,
                                                          @Param("username") String username);

    // 특정 task의 OWNER assignee 엔티티를 조회한다.
    TaskAssigneeEntity findByTaskIdAndRole(Long taskId, TaskRole role);

    // 특정 username이 해당 task assignee인지 확인한다.
    boolean existsByTaskIdAndProjectMemberUserUsername(Long taskId, String username);
}
