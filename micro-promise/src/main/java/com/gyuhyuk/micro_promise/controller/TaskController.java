package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.TaskDTO;
import com.gyuhyuk.micro_promise.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/{projectId}/tasks")
    public TaskDTO createTask(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long parentTaskId,
            @Valid @RequestBody TaskDTO taskDTO,
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return taskService.createTask(taskDTO, parentTaskId, projectId, user.getUsername());
    }
}
