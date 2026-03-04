package com.gyuhyuk.micro_promise.fixture.entity;

import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;

public class ProjectEntityFixture {
    public static ProjectEntity create(String name) {
        return ProjectEntity.builder()
                .name(name)
                .description("This is a sample project.")
                .build();
    }
}
