package com.gyuhyuk.micro_promise.fixture.entity;

import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;

public class ProjectMemberEntityFixture {
    public static ProjectMemberEntity create(String username, ProjectEntity projectEntity, ProjectRole projectRole) {
        return ProjectMemberEntity.builder()
                .user(UserEntityFixture.create(username))
                .project(projectEntity)
                .role(projectRole)
                .build();
    }

    public static ProjectMemberEntity create(UserEntity user, ProjectEntity projectEntity) {
        return ProjectMemberEntity.builder()
                .user(user)
                .project(projectEntity)
                .role(ProjectRole.MEMBER)
                .build();
    }
}
