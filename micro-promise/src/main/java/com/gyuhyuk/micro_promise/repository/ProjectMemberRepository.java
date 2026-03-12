package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMemberEntity, Long> {
    @Query("""
        select distinct pm.project
        from ProjectMemberEntity pm
        where pm.user.username = :username
          and pm.active = true
    """)
    List<ProjectEntity> findProjectsByUsername(@Param("username") String username);

    @Query("""
        select pm
        from ProjectMemberEntity pm
        where pm.project.id = :projectId
          and pm.user.username in :usernames
    """)
    List<ProjectMemberEntity> findProjectMembersByProjectIdAndUsernameIn(
            @Param("projectId") Long projectId,
            @Param("usernames") List<String> usernames
    );

    void deleteByProjectIdAndUserUsername(Long projectId, String username);
    boolean existsByProjectIdAndUserUsername(Long projectId, String username);

    @Query("""
        select pm.role
        from ProjectMemberEntity pm
        where pm.project.id = :projectId
          and pm.user.username = :username
    """)
    ProjectRole findRoleByProjectIdAndUserUsername(Long projectId, String username);

    Optional<ProjectMemberEntity> findByProjectIdAndRoleAndActiveTrue(Long projectId, ProjectRole role);
}
