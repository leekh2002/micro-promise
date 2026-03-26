package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.AcceptProjectInviteRequest;
import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.dto.UserDTO;
import com.gyuhyuk.micro_promise.service.ProjectInviteService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/invite/accept")
    public ProjectDTO acceptInvite(
            @Valid @RequestBody AcceptProjectInviteRequest request,
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setRole(user.getAuthorities().iterator().next().getAuthority());

        return projectInviteService.acceptProjectInvite(userDTO, request.getCode());
    }
}
