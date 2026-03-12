package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskAssigneeDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
import com.gyuhyuk.micro_promise.data.entity.*;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       TaskAssigneeRepository taskAssigneeRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskAssigneeRepository = taskAssigneeRepository;
    }

    public TaskDTO createTask(TaskDTO taskDTO, Long parentTask, Long projectId) {
        List<ProjectMemberEntity> projectMembers= projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(
                projectId,
                taskDTO.getAssignees().stream()
                        .map(TaskAssigneeDTO::getAssigneeName)
                        .collect(Collectors.toList()));

        Optional<String> ownerUsername = taskDTO.getAssignees().stream()
                .filter(a -> Objects.equals(a.getRole(), "OWNER"))
                .map(TaskAssigneeDTO::getAssigneeName)
                .findFirst();


        Optional<ProjectMemberEntity> owner = ownerUsername
                .map(username ->
                        projectMemberRepository
                                .findProjectMembersByProjectIdAndUsernameIn(
                                        projectId,
                                        List.of(username)
                                ).get(0)
                );

        TaskEntity taskEntity = TaskEntity.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(TaskStatus.valueOf(taskDTO.getStatus()))
                .project(projectRepository.findById(projectId).orElseThrow(
                        () -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다.")))
                .progress(0)
                .orderIndex(taskDTO.getOrderIndex())
                .parent(taskRepository.findById(parentTask).orElseThrow(
                        () -> new IllegalArgumentException("부모 태스크를 찾을 수 없습니다.")
                ))
                .build();

        taskRepository.save(taskEntity);

        List<TaskAssigneeEntity> assignees = new ArrayList<>();

        Map<String, String> roleMap =
                taskDTO.getAssignees().stream()
                        .collect(Collectors.toMap(
                                TaskAssigneeDTO::getAssigneeName,
                                TaskAssigneeDTO::getRole
                        ));

        for (ProjectMemberEntity projectMember : projectMembers) {

            String username = projectMember.getUser().getUsername();

            String role = roleMap.get(username);

            TaskAssigneeEntity taskAssigneeEntity = TaskAssigneeEntity.builder()
                    .task(taskEntity)
                    .projectMember(projectMember)
                    .role(TaskRole.valueOf(role))
                    .build();

            assignees.add(taskAssigneeEntity);
        }

        taskAssigneeRepository.saveAll(assignees);
        return TaskDTO.fromEntity(taskEntity, assignees);
    }

    List<TaskDTO> getTasksByProjectId(Long projectId) {
        List<TaskEntity> taskEntities = taskRepository.findByProjectId(projectId);
        List<TaskDTO> taskDTOs = taskEntities.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        return taskDTOs;
    }

    List<TaskDTO> getTasksByAssignee(String username) {
        List<TaskEntity> taskEntities = taskAssigneeRepository.findTaskByAssigneeUsername(username);
        List<TaskDTO> taskDTOs = taskEntities.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        return taskDTOs;
    }

    @Transactional
    TaskDTO updateTask(TaskDTO taskDTO, String requester) {
        TaskEntity taskEntity = taskRepository.findById(taskDTO.getId()).orElseThrow(
                () -> new IllegalArgumentException("태스크를 찾을 수 없습니다.")
        );

        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskDTO.getId(), requester) != TaskRole.OWNER) {
            throw new IllegalArgumentException("태스크 수정 권한이 없습니다.");
        }

        taskEntity.updateTaskInfo(TaskEntity.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(TaskStatus.valueOf(taskDTO.getStatus()))
                .progress(taskDTO.getProgress())
                .orderIndex(taskDTO.getOrderIndex())
                .build());

        return TaskDTO.fromEntity(taskEntity);
    }

    @Transactional
    void deleteTask(Long taskId, String requester) {
        if(!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("태스크를 찾을 수 없습니다.");
        }

        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester) != TaskRole.OWNER) {
            throw new IllegalArgumentException("태스크 삭제 권한이 없습니다.");
        }
        taskRepository.deleteById(taskId);
    }
}
