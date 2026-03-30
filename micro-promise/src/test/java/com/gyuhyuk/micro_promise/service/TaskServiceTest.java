package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.TaskAssigneeDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestCreateRequest;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDecisionRequest;
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
import com.gyuhyuk.micro_promise.data.entity.DoneRequestStatus;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskAssigneeEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskDoneRequestEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.fixture.entity.TaskAssigneeEntityFixture;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
import com.gyuhyuk.micro_promise.repository.TaskDoneRequestRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Mock
    private TaskDoneRequestRepository taskDoneRequestRepository;

    @Test
    void createTask_createsTaskAndAssignees() {
        List<TaskAssigneeDTO> members = new ArrayList<>();
        TaskAssigneeDTO member1 = new TaskAssigneeDTO();
        member1.setAssigneeName("member1");
        member1.setRole("MEMBER");

        TaskAssigneeDTO member2 = new TaskAssigneeDTO();
        member2.setAssigneeName("member2");
        member2.setRole("MEMBER");

        TaskAssigneeDTO owner = new TaskAssigneeDTO();
        owner.setAssigneeName("owner");
        owner.setRole("OWNER");

        members.add(member1);
        members.add(member2);
        members.add(owner);

        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("task");
        taskDTO.setDescription("task description");
        taskDTO.setAssignees(members);
        taskDTO.setStatus("TODO");

        Long projectId = 1L;
        Long parentTaskId = 2L;
        String requester = "owner";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(projectEntity, "id", projectId);

        TaskEntity parentTask = TaskEntity.builder()
                .project(projectEntity)
                .title("parent")
                .description("parent desc")
                .status(TaskStatus.TODO)
                .progress(0)
                .orderIndex(0)
                .build();

        List<ProjectMemberEntity> projectMembers = members.stream()
                .map(assignee -> ProjectMemberEntity.builder()
                        .project(projectEntity)
                        .user(UserEntity.builder()
                                .username(assignee.getAssigneeName())
                                .build())
                        .active(true)
                        .build())
                .toList();

        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requester)).willReturn(true);
        given(projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(
                projectId,
                members.stream().map(TaskAssigneeDTO::getAssigneeName).toList()
        )).willReturn(projectMembers);
        given(taskRepository.findById(parentTaskId)).willReturn(Optional.of(parentTask));
        given(projectRepository.findById(projectId)).willReturn(Optional.of(projectEntity));

        TaskDTO result = taskService.createTask(taskDTO, parentTaskId, projectId, requester);

        assertNotNull(result);
        assertEquals("task", result.getTitle());
        assertEquals(3, result.getAssignees().size());
        verify(taskRepository).save(any(TaskEntity.class));
        verify(taskAssigneeRepository).saveAll(any());
    }

    @Test
    void getTasksByProjectId_returnsTasks() {
        Long projectId = 1L;
        TaskEntity task1 = TaskEntity.builder()
                .title("task1")
                .description("desc1")
                .status(TaskStatus.DOING)
                .build();
        TaskEntity task2 = TaskEntity.builder()
                .title("task2")
                .description("desc2")
                .status(TaskStatus.DOING)
                .build();

        given(taskRepository.findByProjectId(projectId)).willReturn(List.of(task1, task2));

        List<TaskDTO> result = taskService.getTasksByProjectId(projectId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("task1", result.get(0).getTitle());
        assertEquals("task2", result.get(1).getTitle());
    }

    @Test
    void getTasksByAssignee_returnsAssignedTasks() {
        TaskEntity task1 = TaskEntity.builder()
                .title("task1")
                .description("desc1")
                .status(TaskStatus.DOING)
                .build();
        TaskEntity task2 = TaskEntity.builder()
                .title("task2")
                .description("desc2")
                .status(TaskStatus.DOING)
                .build();
        TaskAssigneeEntityFixture.create("member1", task1);
        TaskAssigneeEntityFixture.create("member1", task2);

        given(taskAssigneeRepository.findTaskByAssigneeUsername("member1")).willReturn(List.of(task1, task2));

        List<TaskDTO> result = taskService.getTasksByAssignee("member1");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void updateTask_taskOwnerCanUpdate() {
        Long taskId = 1L;
        String requester = "owner";
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("updated");
        taskDTO.setDescription("updated desc");
        taskDTO.setStatus("DONE");

        given(taskRepository.findById(taskId)).willReturn(Optional.of(TaskEntity.builder()
                .title("existing")
                .description("existing desc")
                .status(TaskStatus.DOING)
                .build()));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester))
                .willReturn(TaskRole.OWNER);

        TaskDTO updatedTaskDTO = taskService.updateTask(taskDTO, requester);

        assertEquals("updated", updatedTaskDTO.getTitle());
    }

    @Test
    void updateTask_nonOwnerCannotUpdate() {
        Long taskId = 1L;
        String requester = "member";
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskId);
        taskDTO.setTitle("updated");
        taskDTO.setDescription("updated desc");
        taskDTO.setStatus("DONE");

        given(taskRepository.findById(taskId)).willReturn(Optional.of(TaskEntity.builder()
                .title("existing")
                .description("existing desc")
                .status(TaskStatus.DOING)
                .build()));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester))
                .willReturn(TaskRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> taskService.updateTask(taskDTO, requester));
    }

    @Test
    void deleteTask_ownerCanDelete() {
        Long taskId = 1L;
        String requester = "owner";
        given(taskRepository.existsById(taskId)).willReturn(true);
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester))
                .willReturn(TaskRole.OWNER);

        taskService.deleteTask(taskId, requester);

        verify(taskRepository).deleteById(taskId);
    }

    @Test
    void deleteTask_nonOwnerCannotDelete() {
        Long taskId = 1L;
        String requester = "member";
        given(taskRepository.existsById(taskId)).willReturn(true);
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester))
                .willReturn(TaskRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> taskService.deleteTask(taskId, requester));
    }

    @Test
    void createTask_nonOwnerCannotCreateRootTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("root");
        taskDTO.setDescription("root desc");
        taskDTO.setStatus("TODO");

        Long projectId = 1L;
        String requester = "member";

        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requester)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, requester)).willReturn(ProjectRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> taskService.createTask(taskDTO, null, projectId, requester));
    }

    @Test
    void createDoneRequest_taskOwnerCanCreateRequestForParentTaskOwner() {
        Long projectId = 1L;
        Long parentTaskId = 10L;
        Long taskId = 20L;
        String requesterUsername = "child-owner";

        ProjectEntity project = ProjectEntity.builder().name("project").description("desc").build();
        ReflectionTestUtils.setField(project, "id", projectId);

        TaskEntity parentTask = TaskEntity.builder()
                .project(project)
                .title("parent")
                .description("parent desc")
                .status(TaskStatus.DOING)
                .progress(70)
                .orderIndex(0)
                .build();
        ReflectionTestUtils.setField(parentTask, "id", parentTaskId);

        TaskEntity childTask = TaskEntity.builder()
                .project(project)
                .parent(parentTask)
                .title("child")
                .description("child desc")
                .status(TaskStatus.DOING)
                .progress(30)
                .orderIndex(1)
                .build();
        ReflectionTestUtils.setField(childTask, "id", taskId);

        ProjectMemberEntity requester = ProjectMemberEntity.builder()
                .project(project)
                .user(UserEntity.builder().username(requesterUsername).build())
                .role(ProjectRole.MEMBER)
                .active(true)
                .build();

        ProjectMemberEntity parentOwner = ProjectMemberEntity.builder()
                .project(project)
                .user(UserEntity.builder().username("parent-owner").build())
                .role(ProjectRole.MEMBER)
                .active(true)
                .build();

        TaskAssigneeEntity parentOwnerAssignee = TaskAssigneeEntity.builder()
                .task(parentTask)
                .projectMember(parentOwner)
                .role(TaskRole.OWNER)
                .build();

        given(taskRepository.findById(taskId)).willReturn(Optional.of(childTask));
        given(projectMemberRepository.findByProjectIdAndUserUsernameAndActiveTrue(projectId, requesterUsername))
                .willReturn(Optional.of(requester));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requesterUsername))
                .willReturn(TaskRole.OWNER);
        given(taskDoneRequestRepository.existsByTaskIdAndStatus(taskId, DoneRequestStatus.REQUESTED))
                .willReturn(false);
        given(taskAssigneeRepository.findByTaskIdAndRole(parentTaskId, TaskRole.OWNER))
                .willReturn(parentOwnerAssignee);
        given(taskDoneRequestRepository.save(any(TaskDoneRequestEntity.class)))
                .willAnswer(invocation -> {
                    TaskDoneRequestEntity entity = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entity, "id", 100L);
                    return entity;
                });

        TaskDoneRequestDTO result = taskService.createDoneRequest(
                projectId,
                taskId,
                new TaskDoneRequestCreateRequest("done plz"),
                requesterUsername
        );

        assertEquals("REQUESTED", result.getStatus());
        assertEquals("parent-owner", result.getTargetTaskOwnerUsername());
        verify(taskDoneRequestRepository).save(any(TaskDoneRequestEntity.class));
    }

    @Test
    void createDoneRequest_nonOwnerCannotCreate() {
        Long projectId = 1L;
        Long taskId = 2L;
        String requesterUsername = "member";

        ProjectEntity project = ProjectEntity.builder().name("project").description("desc").build();
        ReflectionTestUtils.setField(project, "id", projectId);

        TaskEntity task = TaskEntity.builder()
                .project(project)
                .title("task")
                .description("desc")
                .status(TaskStatus.DOING)
                .progress(30)
                .orderIndex(0)
                .build();
        ReflectionTestUtils.setField(task, "id", taskId);

        ProjectMemberEntity requester = ProjectMemberEntity.builder()
                .project(project)
                .user(UserEntity.builder().username(requesterUsername).build())
                .role(ProjectRole.MEMBER)
                .active(true)
                .build();

        given(taskRepository.findById(taskId)).willReturn(Optional.of(task));
        given(projectMemberRepository.findByProjectIdAndUserUsernameAndActiveTrue(projectId, requesterUsername))
                .willReturn(Optional.of(requester));
        given(taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requesterUsername))
                .willReturn(TaskRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> taskService.createDoneRequest(
                projectId,
                taskId,
                new TaskDoneRequestCreateRequest("done plz"),
                requesterUsername
        ));
    }

    @Test
    void decideDoneRequest_approveMarksTaskDone() {
        Long projectId = 1L;
        Long taskId = 2L;
        Long doneRequestId = 3L;

        ProjectEntity project = ProjectEntity.builder().name("project").description("desc").build();
        ReflectionTestUtils.setField(project, "id", projectId);

        TaskEntity task = TaskEntity.builder()
                .project(project)
                .title("task")
                .description("desc")
                .status(TaskStatus.DOING)
                .progress(50)
                .orderIndex(0)
                .build();
        ReflectionTestUtils.setField(task, "id", taskId);

        ProjectMemberEntity requester = ProjectMemberEntity.builder()
                .project(project)
                .user(UserEntity.builder().username("child-owner").build())
                .role(ProjectRole.MEMBER)
                .active(true)
                .build();

        ProjectMemberEntity parentOwner = ProjectMemberEntity.builder()
                .project(project)
                .user(UserEntity.builder().username("parent-owner").build())
                .role(ProjectRole.MEMBER)
                .active(true)
                .build();

        TaskDoneRequestEntity doneRequest = TaskDoneRequestEntity.builder()
                .task(task)
                .requester(requester)
                .targetTaskOwner(parentOwner)
                .status(DoneRequestStatus.REQUESTED)
                .message("please approve")
                .build();
        ReflectionTestUtils.setField(doneRequest, "id", doneRequestId);

        given(taskDoneRequestRepository.findById(doneRequestId)).willReturn(Optional.of(doneRequest));

        TaskDoneRequestDTO result = taskService.decideDoneRequest(
                projectId,
                taskId,
                doneRequestId,
                new TaskDoneRequestDecisionRequest(true),
                "parent-owner"
        );

        assertEquals("ACCEPTED", result.getStatus());
        assertEquals("DONE", task.getStatus().name());
        assertEquals(100, task.getProgress());
        assertTrue(result.getDecidedAt() != null);
    }
}
