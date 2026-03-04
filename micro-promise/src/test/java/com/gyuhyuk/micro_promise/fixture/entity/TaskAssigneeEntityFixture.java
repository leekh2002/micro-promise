package com.gyuhyuk.micro_promise.fixture.entity;

import com.gyuhyuk.micro_promise.data.entity.*;

public class TaskAssigneeEntityFixture {
    public static TaskAssigneeEntity create(ProjectMemberEntity member, String projectTitle) {
        ProjectEntity project = ProjectEntityFixture.create(projectTitle);


        return TaskAssigneeEntity.builder()
                .projectMember(member)
                .task(TaskEntityFixture.create(
                        project,
                        null,
                        "abcd",
                        TaskStatus.DOING,
                        1))
                .role(TaskRole.MEMBER)
                .build();
    }

    public static TaskAssigneeEntity create(ProjectMemberEntity member, TaskEntity task) {
        return TaskAssigneeEntity.builder()
                .projectMember(member)
                .task(task)
                .role(TaskRole.MEMBER)
                .build();
    }

    public static TaskAssigneeEntity create(String username, TaskEntity task) {
        ProjectMemberEntity member = ProjectMemberEntityFixture.create(username, task.getProject(), ProjectRole.MEMBER);
        return TaskAssigneeEntity.builder()
                .projectMember(member)
                .task(task)
                .role(TaskRole.MEMBER)
                .build();
    }
}
