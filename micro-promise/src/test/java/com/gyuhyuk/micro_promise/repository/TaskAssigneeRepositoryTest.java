package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.*;
import com.gyuhyuk.micro_promise.fixture.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TaskAssigneeRepositoryTest {
    @Autowired
    private TaskAssigneeRepository taskAssigneeRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findTaskByAssigneeUsername_테스트() {
        //given
        String username = "testuser";
        ProjectEntity project = ProjectEntityFixture.create("test project");


        UserEntity user = UserEntityFixture.create(username);
        UserEntity owner = UserEntityFixture.create("owner");


        ProjectMemberEntity projectMember = ProjectMemberEntityFixture.create(user, project);


        ProjectMemberEntity ownerMember = ProjectMemberEntityFixture.create(owner, project);



        TaskEntity task1 = TaskEntityFixture.create(project, "task1");
        TaskEntity task2 = TaskEntityFixture.create(project, "task2");


        TaskAssigneeEntity assignee1 = TaskAssigneeEntityFixture.create(projectMember, task1);
        TaskAssigneeEntity assignee2 = TaskAssigneeEntityFixture.create(projectMember, task2);

        projectRepository.save(project);
        userRepository.save(user);
        userRepository.save(owner);
        projectMemberRepository.save(projectMember);
        projectMemberRepository.save(ownerMember);
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskAssigneeRepository.save(assignee1);
        taskAssigneeRepository.save(assignee2);

        //when
        List<TaskEntity> tasksForUser1 = taskAssigneeRepository.findTaskByAssigneeUsername(username);

        //then
        assertEquals(2, tasksForUser1.size());

    }

}