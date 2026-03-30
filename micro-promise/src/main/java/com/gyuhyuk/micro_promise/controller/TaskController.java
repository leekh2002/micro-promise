package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestCreateRequest;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDTO;
import com.gyuhyuk.micro_promise.data.dto.TaskDoneRequestDecisionRequest;
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

    // task 완료 요청 생성 API다.
    @PostMapping("/{projectId}/tasks/{taskId}/done-requests")
    public TaskDoneRequestDTO createDoneRequest(
            // 경로에서 프로젝트 ID를 받는다.
            @PathVariable Long projectId,
            // 경로에서 task ID를 받는다.
            @PathVariable Long taskId,
            // 요청 본문에서 완료 요청 메시지를 받는다.
            @RequestBody(required = false) TaskDoneRequestCreateRequest request,
            // 인증된 사용자 정보를 주입받는다.
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        // 본문이 없으면 message가 null인 기본 요청 객체를 만든다.
        TaskDoneRequestCreateRequest doneRequest = request == null
                ? new TaskDoneRequestCreateRequest(null)
                : request;
        // 서비스에 완료 요청 생성을 위임한다.
        return taskService.createDoneRequest(projectId, taskId, doneRequest, user.getUsername());
    }

    // 완료 요청 승인/거절 API다.
    @PostMapping("/{projectId}/tasks/{taskId}/done-requests/{doneRequestId}/decision")
    public TaskDoneRequestDTO decideDoneRequest(
            // 경로에서 프로젝트 ID를 받는다.
            @PathVariable Long projectId,
            // 경로에서 task ID를 받는다.
            @PathVariable Long taskId,
            // 경로에서 완료 요청 ID를 받는다.
            @PathVariable Long doneRequestId,
            // 요청 본문에서 승인 여부를 받는다.
            @RequestBody TaskDoneRequestDecisionRequest request,
            // 인증된 사용자 정보를 주입받는다.
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        // 서비스에 완료 요청 처리 로직을 위임한다.
        return taskService.decideDoneRequest(projectId, taskId, doneRequestId, request, user.getUsername());
    }
}
