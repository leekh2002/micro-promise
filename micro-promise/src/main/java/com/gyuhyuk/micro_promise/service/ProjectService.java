package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createProject(ProjectDTO projectDTO) {
        if (projectDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null");
        }

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name(projectDTO.getName())
                .description(projectDTO.getDescription())
                .build();

        projectRepository.save(projectEntity);
    }

    public List<ProjectDTO> getProjectsByUsername(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User does not exist");
        }

        List<ProjectEntity> projects = projectMemberRepository.findProjectsByUsername(username);

        List<ProjectDTO> projectDTOs = new ArrayList<>();
        for (ProjectEntity project : projects) {
            ProjectDTO dto = new ProjectDTO();
            dto.setId(project.getId());
            dto.setName(project.getName());
            dto.setDescription(project.getDescription());
            dto.setCreatedAt(project.getCreatedAt());
            projectDTOs.add(dto);
        }

        return projectDTOs;
    }

    @Transactional
    public ProjectDTO updateProject(ProjectDTO projectDTO, String requestedUsername) {
        if (projectDTO.getId() == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }

        if (!projectRepository.existsById(projectDTO.getId())) {
            throw new IllegalArgumentException("Project does not exist");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectDTO.getId(), requestedUsername)) {
            throw new IllegalArgumentException("User is not a member of the project");
        }

        if (projectMemberRepository.findRoleByProjectIdAndUserUsername(projectDTO.getId(), requestedUsername) != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Only project owners can update project information");
        }

        ProjectEntity projectEntity = projectRepository.findById(projectDTO.getId()).orElseThrow();
        projectEntity.updateProjectInfo(projectDTO.getName(), projectDTO.getDescription());

        ProjectEntity updatedEntity = projectRepository.save(projectEntity);
        ProjectDTO updatedDTO = new ProjectDTO();
        updatedDTO.setId(updatedEntity.getId());
        updatedDTO.setName(updatedEntity.getName());
        updatedDTO.setDescription(updatedEntity.getDescription());
        updatedDTO.setCreatedAt(updatedEntity.getCreatedAt());

        return updatedDTO;


    }

    @Transactional
    public void deleteProject(Long projectId, String requestedUsername) {
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project does not exist");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requestedUsername)) {
            throw new IllegalArgumentException("User is not a member of the project");
        }

        if (projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, requestedUsername) != ProjectRole.OWNER) {
            throw new IllegalArgumentException("Only project owners can delete the project");
        }

        projectRepository.deleteById(projectId);
    }
}
