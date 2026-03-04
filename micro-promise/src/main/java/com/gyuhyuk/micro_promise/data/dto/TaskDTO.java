package com.gyuhyuk.micro_promise.data.dto;

import com.gyuhyuk.micro_promise.data.entity.TaskAssigneeEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskEntity;
import com.gyuhyuk.micro_promise.data.entity.TaskStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private int orderIndex;
    private List<TaskAssigneeDTO> assignees;
    private int progress;

    public static TaskDTO fromEntity(TaskEntity taskEntity, List<TaskAssigneeEntity> assignees) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskEntity.getId());
        taskDTO.setTitle(taskEntity.getTitle());
        taskDTO.setDescription(taskEntity.getDescription());
        taskDTO.setStatus(taskEntity.getStatus().name());
        taskDTO.setOrderIndex(taskEntity.getOrderIndex());
        taskDTO.setProgress(taskEntity.getProgress());
        taskDTO.setAssignees(
                assignees.stream()
                        .map(assignee -> {
                            TaskAssigneeDTO assigneeDTO = new TaskAssigneeDTO();
                            assigneeDTO.setAssigneeName(assignee.getProjectMember().getUser().getUsername());
                            assigneeDTO.setRole(assignee.getRole().name());
                            return assigneeDTO;
                        })
                        .toList()
        );
        return taskDTO;
    }

    public static TaskDTO fromEntity(TaskEntity taskEntity) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(taskEntity.getId());
        taskDTO.setTitle(taskEntity.getTitle());
        taskDTO.setDescription(taskEntity.getDescription());
        taskDTO.setStatus(taskEntity.getStatus().name());
        taskDTO.setOrderIndex(taskEntity.getOrderIndex());
        taskDTO.setProgress(taskEntity.getProgress());

        return taskDTO;
    }
}

