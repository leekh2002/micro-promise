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
    // 현재 사용자가 active 상태로 참여 중인 프로젝트 목록만 조회한다.
    @Query("""
        select distinct pm.project
        from ProjectMemberEntity pm
        where pm.user.username = :username
          and pm.active = true
    """)
    List<ProjectEntity> findProjectsByUsername(@Param("username") String username);

    // 같은 프로젝트 안에서 특정 username 목록에 해당하는 프로젝트 멤버 엔티티를 한 번에 조회한다.
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

    // 프로젝트에서 특정 사용자를 제거할 때 쓰는 삭제 메서드다.
    void deleteByProjectIdAndUserUsername(Long projectId, String username);
    // 특정 사용자가 프로젝트 멤버인지 빠르게 존재 여부만 확인할 때 쓴다.
    boolean existsByProjectIdAndUserUsername(Long projectId, String username);

    // 프로젝트 안에서 특정 사용자의 프로젝트 역할(OWNER/MEMBER)을 조회한다.
    @Query("""
        select pm.role
        from ProjectMemberEntity pm
        where pm.project.id = :projectId
          and pm.user.username = :username
    """)
    ProjectRole findRoleByProjectIdAndUserUsername(Long projectId, String username);

    // 프로젝트의 active owner 멤버를 조회한다.
    Optional<ProjectMemberEntity> findByProjectIdAndRoleAndActiveTrue(Long projectId, ProjectRole role);
    // 프로젝트 안에서 특정 username의 active 멤버 엔티티를 조회한다.
    // 수동 Git 링크처럼 "현재 요청자"의 실제 멤버 엔티티가 필요한 경우 사용한다.
    Optional<ProjectMemberEntity> findByProjectIdAndUserUsernameAndActiveTrue(Long projectId, String username);
}
