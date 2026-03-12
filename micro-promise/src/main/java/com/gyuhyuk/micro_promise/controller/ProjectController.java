package com.gyuhyuk.micro_promise.controller;

import com.gyuhyuk.micro_promise.data.dto.CustomOAuth2User;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ProjectDTO createProject(
            @Valid @RequestBody ProjectDTO projectDTO,
            @AuthenticationPrincipal CustomOAuth2User user
    ) {
        return projectService.createProject(projectDTO, user.getUsername());
    }

    @GetMapping
    public List<ProjectDTO> getProjects(@AuthenticationPrincipal CustomOAuth2User user) {
        return projectService.getProjectsByUsername(user.getUsername());
    }
}
