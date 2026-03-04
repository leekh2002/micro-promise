package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository, ProjectRepository projectRepository) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public void deleteProjectMember(Long projectId, String username) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project with id " + projectId + " does not exist.");
        }
        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username)) {
            throw new IllegalArgumentException("User " + username + " is not a member of project.");
        }

        projectMemberRepository.deleteByProjectIdAndUserUsername(projectId, username);
    }

}
