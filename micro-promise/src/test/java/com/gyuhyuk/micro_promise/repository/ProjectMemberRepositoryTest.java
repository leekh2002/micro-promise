package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProjectMemberRepositoryTest {
    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findProjectMembersByProjectIdAndUsernameIn_테스트() {
        // given
        Long projectId = 1L;
        String username1 = "user1";
        String username2 = "user2";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("Test Project")
                .build();

        projectRepository.save(projectEntity);

        UserEntity user1 = UserEntity.builder()
                .username(username1)
                .name(username1)
                .build();
        UserEntity user2 = UserEntity.builder()
                .username(username2)
                .name(username2)
                .build();

        userRepository.saveAll(List.of(user1, user2));

        ProjectMemberEntity projectMemberEntity1 = ProjectMemberEntity.builder()
                .project(projectEntity)
                .active(true)
                .user(user1)
                .role(ProjectRole.MEMBER)
                .build();

        ProjectMemberEntity projectMemberEntity2 = ProjectMemberEntity.builder()
                .project(projectEntity)
                .active(true)
                .user(user2)
                .role(ProjectRole.MEMBER)
                .build();

        projectMemberRepository.saveAll(List.of(projectMemberEntity1, projectMemberEntity2));

        // when
        var result = projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(projectId, List.of(username1, username2));

        // then
        assertNotNull(result);
        assertArrayEquals(new String[]{username1, username2}, result.stream().map(pm -> pm.getUser().getUsername()).toArray());
    }
}