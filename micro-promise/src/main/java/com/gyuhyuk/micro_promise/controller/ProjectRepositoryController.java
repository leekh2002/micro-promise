package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.ConnectRepositoryRequest;
import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.service.GitRepositoryService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectRepositoryController {

    private final GitRepositoryService gitRepositoryService;

    public ProjectRepositoryController(GitRepositoryService gitRepositoryService) {
        this.gitRepositoryService = gitRepositoryService;
    }

    @PostMapping("/{projectId}/repository")
    public GitHubRepositoryResponse connectRepository(
            @PathVariable Long projectId,
            @Valid @RequestBody ConnectRepositoryRequest request,
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return gitRepositoryService.connectRepository(projectId, request.repositoryUrl(), user.getUsername());
    }
}
