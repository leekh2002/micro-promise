package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.LinkBranchRequest;
import com.gyuhyuk.micro_promise.data.dto.LinkCommitRequest;
import com.gyuhyuk.micro_promise.service.TaskGitLinkService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// task 하위 리소스로 Git branch/commit 수동 연동 API를 제공한다.
@RequestMapping("/tasks")
public class TaskGitLinkController {

    // 실제 링크 생성과 권한 검증은 서비스에 위임한다.
    private final TaskGitLinkService taskGitLinkService;

    // 컨트롤러에 필요한 서비스 의존성을 생성자 주입으로 받는다.
    public TaskGitLinkController(TaskGitLinkService taskGitLinkService) {
        // 서비스 참조를 필드에 저장한다.
        this.taskGitLinkService = taskGitLinkService;
    }

    // 사용자가 선택한 branch id를 특정 task에 수동으로 연결한다.
    @PostMapping("/{taskId}/branches")
    public void linkBranch(
            // URL 경로에서 대상 task id를 받는다.
            @PathVariable Long taskId,
            // 요청 본문에서 연결할 branch id를 받는다.
            @Valid @RequestBody LinkBranchRequest request,
            // 인증된 현재 사용자를 받아 권한 검증에 사용한다.
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        // 서비스 계층으로 task id, branch id, 요청자 username을 전달한다.
        taskGitLinkService.linkBranch(taskId, request.branchId(), user.getUsername());
    }

    // 사용자가 선택한 commit sha를 특정 task에 수동으로 연결한다.
    @PostMapping("/{taskId}/commits")
    public void linkCommit(
            // URL 경로에서 대상 task id를 받는다.
            @PathVariable Long taskId,
            // 요청 본문에서 연결할 commit sha를 받는다.
            @Valid @RequestBody LinkCommitRequest request,
            // 인증된 현재 사용자를 받아 권한 검증에 사용한다.
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        // 서비스 계층으로 task id, commit sha, 요청자 username을 전달한다.
        taskGitLinkService.linkCommit(taskId, request.commitSha(), user.getUsername());
    }
}
