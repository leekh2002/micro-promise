package com.gyuhyuk.micro_promise.fixture.entity;

import com.gyuhyuk.micro_promise.data.entity.*;

public class TaskEntityFixture {
    public static TaskEntity create(ProjectEntity projectEntity,
                                    TaskEntity parent,
                                    String title,
                                    TaskStatus status,
                                    int orderIndex) {
        return TaskEntity.builder()
                .project(projectEntity)
                .parent(parent)
                .title(title)
                .description("This is a sample project.")
                .status(status)
                .progress(0)
                .orderIndex(orderIndex)
                .build();
    }

    public static TaskEntity create() {
        ProjectEntity project = ProjectEntityFixture.create("Sample Project");
        return TaskEntity.builder()
                .project(ProjectEntityFixture.create("Sample Project"))
                .parent(null)
                .title("Sample Task")
                .description("This is a sample project.")
                .status(TaskStatus.TODO)
                .progress(0)
                .orderIndex(0)
                .build();
    }

    public static TaskEntity create(ProjectEntity projectEntity, String title) {
        return TaskEntity.builder()
                .project(projectEntity)
                .parent(null)
                .title(title)
                .description("This is a sample project.")
                .status(TaskStatus.TODO)
                .progress(0)
                .orderIndex(0)
                .build();
    }
}
