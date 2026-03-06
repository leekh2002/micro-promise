package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.TaskAssigneeDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
import com.gyuhyuk.micro_promise.data.entity.*;
import com.gyuhyuk.micro_promise.fixture.entity.TaskAssigneeEntityFixture;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Test
    void createTask_테스트() {
        //given
        List<TaskAssigneeDTO> members = new ArrayList<>();
        TaskAssigneeDTO taskAssigneeDTO = new TaskAssigneeDTO();
        taskAssigneeDTO.setAssigneeName("테스트 유저");
        taskAssigneeDTO.setRole("MEMBER");

        TaskAssigneeDTO taskAssigneeDTO2 = new TaskAssigneeDTO();
        taskAssigneeDTO2.setAssigneeName("테스트 유저2");
        taskAssigneeDTO2.setRole("MEMBER");

        TaskAssigneeDTO ownerAssigneeDTO = new TaskAssigneeDTO();
        ownerAssigneeDTO.setAssigneeName("테스트 owner");
        ownerAssigneeDTO.setRole("OWNER");

        members.add(taskAssigneeDTO);
        members.add(taskAssigneeDTO2);
        members.add(ownerAssigneeDTO);

//        members.add("테스트 유저");
//        members.add("테스트 유저2");
                //List.of("테스트 유저", "테스트 유저2");


        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("테스트 태스크");
        taskDTO.setDescription("테스트 태스크 설명");
        taskDTO.setAssignees(members);
        taskDTO.setStatus("TODO");

        Long parentTaskId = 2L;
        TaskEntity parentTask = TaskEntity.builder()
                .title("부모 태스크")
                .description("부모 태스크 설명")
                .build();

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("테스트 프로젝트")
                .description("테스트 프로젝트 설명")
                .build();

        ReflectionTestUtils.setField(projectEntity, "id", 1L);

        UserEntity owner = UserEntity.builder()
                .username("테스트 owner")
                .build();

        List<UserEntity> userEntities = members.stream()
                .map(name -> UserEntity.builder()
                        .username(name.getAssigneeName())
                        .build())
                .toList();

        List<ProjectMemberEntity> projectMembers = new ArrayList<>(
                userEntities.stream()
                        .map(user -> ProjectMemberEntity.builder()
                                .user(user)
                                .build())
                        .toList()
        );
        given(projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(1L, members.stream().map(TaskAssigneeDTO::getAssigneeName).toList()))
                .willReturn(projectMembers);
        given(projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(1L, List.of("테스트 owner")))
                .willReturn(List.of(ProjectMemberEntity.builder()
                        .user(owner)
                        .build()));
        given(projectRepository.findById(1L)).willReturn(java.util.Optional.of(projectEntity));
        given(taskRepository.findById(2L)).willReturn(Optional.ofNullable(parentTask));




        //when
        TaskDTO result = taskService.createTask(taskDTO, 2L, 1L);

        //then
        assertNotNull(result);
        assertEquals("테스트 태스크", result.getTitle());
        assertArrayEquals(new String[]{"테스트 유저", "테스트 유저2", "테스트 owner"},
                result.getAssignees().stream().map(TaskAssigneeDTO::getAssigneeName).toArray());
        verify(taskRepository).save(org.mockito.ArgumentMatchers.any(TaskEntity.class));
        verify(taskAssigneeRepository).saveAll(org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getTasksByProjectId_테스트() {
        //given
        Long projectId = 1L;
        TaskEntity task1 = TaskEntity.builder()
                .title("태스크 1")
                .description("태스크 1 설명")
                .status(TaskStatus.DOING)
                .build();
        TaskEntity task2 = TaskEntity.builder()
                .title("태스크 2")
                .description("태스크 2 설명")
                .status(TaskStatus.DOING)
                .build();

        List<TaskEntity> tasks = List.of(task1, task2);
        given(taskRepository.findByProjectId(projectId)).willReturn(tasks);

        //when
        List<TaskDTO> result = taskService.getTasksByProjectId(projectId);

        //then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("태스크 1", result.get(0).getTitle());
        assertEquals("태스크 2", result.get(1).getTitle());
    }

    @Test
    void getTasksByAssignee_테스트() {
        //given
        Long projectId = 1L;
        TaskEntity task1 = TaskEntity.builder()
                .title("태스크 1")
                .description("태스크 1 설명")
                .status(TaskStatus.DOING)
                .build();
        TaskEntity task2 = TaskEntity.builder()
                .title("태스크 2")
                .description("태스크 2 설명")
                .status(TaskStatus.DOING)
                .build();
        TaskEntity task3 = TaskEntity.builder()
                .title("태스크 3")
                .description("태스크 3 설명")
                .status(TaskStatus.DOING)
                .build();
        TaskAssigneeEntity assignee1 = TaskAssigneeEntityFixture.create("테스트 유저", task1);
        TaskAssigneeEntity assignee2 = TaskAssigneeEntityFixture.create("테스트 유저", task3);
        TaskAssigneeEntity assignee3 = TaskAssigneeEntityFixture.create("테스트 유저2", task2);

        List<TaskEntity> tasks = List.of(task1, task3);

        given(taskAssigneeRepository.findTaskByAssigneeUsername("테스트 유저")).willReturn(tasks);

        //when
        List<TaskDTO> result = taskService.getTasksByAssignee("테스트 유저");

        //then
        assertNotNull(result);
        assertEquals(2, result.size());

    }

    @Test
    void updateTask_TaskOwner_테스트() {
        //given
        Long taskId = 1L;
        String requester = "테스트 owner";
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("업데이트된 태스크");
        taskDTO.setDescription("업데이트된 태스크 설명");
        taskDTO.setStatus("DONE");

        TaskAssigneeDTO assigneeDTO = new TaskAssigneeDTO();
        assigneeDTO.setAssigneeName("업데이트된 유저");
        assigneeDTO.setRole("MEMBER");

        taskDTO.setAssignees(List.of(assigneeDTO));

        given(taskRepository.findById(taskId)).willReturn(Optional.of(TaskEntity.builder()
                .title("기존 태스크")
                .description("기존 태스크 설명")
                .status(TaskStatus.DOING)
                .build()));

        given(taskAssigneeRepository.findRoleByTaskIdAndAssigneeUsername(taskId, requester))
                .willReturn(TaskRole.OWNER);

        //when
        TaskDTO updatedTaskDTO = taskService.updateTask(taskDTO, requester);

        //then
        assertEquals("업데이트된 태스크", updatedTaskDTO.getTitle());
    }

    @Test
    void updateTask_NotOwner_테스트() {
        //given
        Long taskId = 1L;
        String requester = "테스트 유저";
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("업데이트된 태스크");
        taskDTO.setDescription("업데이트된 태스크 설명");
        taskDTO.setStatus("DONE");

        given(taskRepository.findById(taskId)).willReturn(Optional.of(TaskEntity.builder()
                .title("기존 태스크")
                .description("기존 태스크 설명")
                .status(TaskStatus.DOING)
                .build()));

        given(taskAssigneeRepository.findRoleByTaskIdAndAssigneeUsername(taskId, requester))
                .willReturn(TaskRole.MEMBER);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(taskDTO, requester);
        });
    }

    @Test
    void deleteTask_Owner_테스트() {
        //given
        Long taskId = 1L;
        String requester = "테스트 owner";
        given(taskRepository.existsById(taskId)).willReturn(true);

        given(taskAssigneeRepository.findRoleByTaskIdAndAssigneeUsername(taskId, requester))
                .willReturn(TaskRole.OWNER);

        //when
        taskService.deleteTask(taskId, requester);

        //then
        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_NotOwner_테스트() {
        //given
        Long taskId = 1L;
        String requester = "테스트 유저";

        given(taskRepository.existsById(taskId)).willReturn(true);
        given(taskAssigneeRepository.findRoleByTaskIdAndAssigneeUsername(taskId, requester))
                .willReturn(TaskRole.MEMBER);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.deleteTask(taskId, requester);
        });

    }

}