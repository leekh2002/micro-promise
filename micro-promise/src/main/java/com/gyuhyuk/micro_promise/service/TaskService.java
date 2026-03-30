package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.TaskAssigneeDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestCreateRequest;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDecisionRequest;
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
import com.gyuhyuk.micro_promise.data.entity.DoneRequestStatus;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.TaskAssigneeEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskDoneRequestEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskRole;
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.TaskAssigneeRepository;
import com.gyuhyuk.micro_promise.repository.TaskDoneRequestRepository;
import com.gyuhyuk.micro_promise.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskDoneRequestRepository taskDoneRequestRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       ProjectMemberRepository projectMemberRepository,
                       TaskAssigneeRepository taskAssigneeRepository,
                       TaskDoneRequestRepository taskDoneRequestRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskAssigneeRepository = taskAssigneeRepository;
        this.taskDoneRequestRepository = taskDoneRequestRepository;
    }

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, Long parentTaskId, Long projectId, String requester) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project does not exist");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requester)) {
            throw new IllegalArgumentException("User is not a member of the project");
        }

        if (parentTaskId == null
                && projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, requester) != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Only the project owner can create a root task");
        }

        List<TaskAssigneeDTO> requestedAssignees = taskDTO.getAssignees() != null ? taskDTO.getAssignees() : List.of();
        List<String> assigneeUsernames = requestedAssignees.stream()
                .map(TaskAssigneeDTO::getAssigneeName)
                .filter(Objects::nonNull)
                .toList();

        List<ProjectMemberEntity> projectMembers = assigneeUsernames.isEmpty()
                ? List.of()
                : projectMemberRepository.findProjectMembersByProjectIdAndUsernameIn(projectId, assigneeUsernames);

        if (projectMembers.size() != assigneeUsernames.size()) {
            throw new IllegalArgumentException("All assignees must be active project members");
        }

        TaskEntity parentEntity = parentTaskId == null
                ? null
                : taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new IllegalArgumentException("Parent task does not exist"));

        if (parentEntity != null && !Objects.equals(parentEntity.getProject().getId(), projectId)) {
            throw new IllegalArgumentException("Parent task must belong to the same project");
        }

        TaskEntity taskEntity = TaskEntity.builder()
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .status(TaskStatus.valueOf(taskDTO.getStatus()))
                .project(projectRepository.findById(projectId)
                        .orElseThrow(() -> new IllegalArgumentException("Project does not exist")))
                .progress(0)
                .orderIndex(taskDTO.getOrderIndex())
                .parent(parentEntity)
                .build();

        taskRepository.save(taskEntity);

        List<TaskAssigneeEntity> assignees = new ArrayList<>();
        Map<String, String> roleMap = requestedAssignees.stream()
                .collect(Collectors.toMap(
                        TaskAssigneeDTO::getAssigneeName,
                        TaskAssigneeDTO::getRole,
                        (left, right) -> right,
                        LinkedHashMap::new
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
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    List<TaskDTO> getTasksByAssignee(String username) {
        return taskAssigneeRepository.findTaskByAssigneeUsername(username).stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    TaskDTO updateTask(TaskDTO taskDTO, String requester) {
        TaskEntity taskEntity = taskRepository.findById(taskDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Task does not exist"));

        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskDTO.getId(), requester) != TaskRole.OWNER) {
            throw new IllegalArgumentException("User cannot update this task");
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
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task does not exist");
        }

        if (taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requester) != TaskRole.OWNER) {
            throw new IllegalArgumentException("User cannot delete this task");
        }

        taskRepository.deleteById(taskId);
    }

    @Transactional
    public TaskDoneRequestDTO createDoneRequest(Long projectId, Long taskId, TaskDoneRequestCreateRequest request, String requesterUsername) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task does not exist"));

        if (!Objects.equals(task.getProject().getId(), projectId)) {
            throw new IllegalArgumentException("Task does not belong to the project");
        }

        ProjectMemberEntity requester = projectMemberRepository
                .findByProjectIdAndUserUsernameAndActiveTrue(projectId, requesterUsername)
                .orElseThrow(() -> new IllegalArgumentException("User is not an active project member"));

        TaskRole requesterTaskRole = taskAssigneeRepository.findRoleByTaskIdAndProjectMemberUserUsername(taskId, requesterUsername);
        log.info("createDoneRequest auth check: projectId={}, taskId={}, requesterUsername={}, projectMemberId={}, requesterTaskRole={}",
                projectId, taskId, requesterUsername, requester.getId(), requesterTaskRole);

        if (requesterTaskRole != TaskRole.OWNER) {
            log.warn("createDoneRequest denied: projectId={}, taskId={}, requesterUsername={}, requesterTaskRole={}",
                    projectId, taskId, requesterUsername, requesterTaskRole);
            throw new IllegalArgumentException("Only the task owner can create a done request");
        }

        if (task.getStatus() == TaskStatus.DONE) {
            throw new IllegalArgumentException("Task is already done");
        }

        if (taskDoneRequestRepository.existsByTaskIdAndStatus(taskId, DoneRequestStatus.REQUESTED)) {
            throw new IllegalArgumentException("Task already has a pending done request");
        }

        ProjectMemberEntity targetTaskOwner = resolveParentTaskOwner(task);

        TaskDoneRequestEntity doneRequest = taskDoneRequestRepository.save(TaskDoneRequestEntity.builder()
                .task(task)
                .requester(requester)
                .targetTaskOwner(targetTaskOwner)
                .status(DoneRequestStatus.REQUESTED)
                .message(request.message())
                .build());

        return TaskDoneRequestDTO.fromEntity(doneRequest);
    }

    @Transactional
    public TaskDoneRequestDTO decideDoneRequest(Long projectId, Long taskId, Long doneRequestId,
                                                TaskDoneRequestDecisionRequest request, String requesterUsername) {
        TaskDoneRequestEntity doneRequest = taskDoneRequestRepository.findById(doneRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Done request does not exist"));

        if (!Objects.equals(doneRequest.getTask().getId(), taskId)) {
            throw new IllegalArgumentException("Done request does not belong to the task");
        }

        if (!Objects.equals(doneRequest.getTask().getProject().getId(), projectId)) {
            throw new IllegalArgumentException("Done request does not belong to the project");
        }

        if (doneRequest.getStatus() != DoneRequestStatus.REQUESTED) {
            throw new IllegalArgumentException("Done request is already processed");
        }

        if (!Objects.equals(doneRequest.getTargetTaskOwner().getUser().getUsername(), requesterUsername)) {
            throw new IllegalArgumentException("User cannot process this done request");
        }

        if (request.approve()) {
            doneRequest.accept();
            doneRequest.getTask().updateTaskInfo(TaskEntity.builder()
                    .status(TaskStatus.DONE)
                    .progress(100)
                    .build());
        } else {
            doneRequest.reject();
        }

        return TaskDoneRequestDTO.fromEntity(doneRequest);
    }

    private ProjectMemberEntity resolveParentTaskOwner(TaskEntity task) {
        TaskEntity parentTask = task.getParent();
        if (parentTask == null) {
            throw new IllegalArgumentException("Parent task does not exist");
        }

        TaskAssigneeEntity parentOwnerAssignee = taskAssigneeRepository.findByTaskIdAndRole(parentTask.getId(), TaskRole.OWNER);
        if (parentOwnerAssignee == null) {
            throw new IllegalArgumentException("Parent task owner does not exist");
        }

        return parentOwnerAssignee.getProjectMember();
    }
}
