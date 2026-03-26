package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.service.ProjectInviteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectInviteController {

    private final ProjectInviteService projectInviteService;

    public ProjectInviteController(ProjectInviteService projectInviteService) {
        this.projectInviteService = projectInviteService;
    }

    @PostMapping("/{projectId}/invite-code")
    public String generateInviteCode(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return projectInviteService.generateInviteCode(projectId, user.getUsername());
    }
}
