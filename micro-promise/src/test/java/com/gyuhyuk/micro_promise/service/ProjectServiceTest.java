package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @InjectMocks
    private ProjectService projectService;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createProject_savesProject() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("project1");
        projectDTO.setDescription("desc1");

        ProjectEntity projectEntity = new ProjectEntity("project1", "desc1", null);
        ReflectionTestUtils.setField(projectEntity, "id", 1L);
        given(projectRepository.save(any(ProjectEntity.class))).willReturn(projectEntity);

        projectService.createProject(projectDTO);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void createProject_registersOwnerMember() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("project");
        projectDTO.setDescription("desc");
        String ownerUsername = "GITHUB 100";
        UserEntity owner = UserEntity.builder()
                .username(ownerUsername)
                .email("owner@test.com")
                .name("owner")
                .build();

        given(userRepository.findByUsername(ownerUsername)).willReturn(owner);
        given(projectRepository.save(any(ProjectEntity.class)))
                .willAnswer(invocation -> {
                    ProjectEntity entity = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entity, "id", 1L);
                    return entity;
                });

        ProjectDTO created = projectService.createProject(projectDTO, ownerUsername);

        assertEquals(1L, created.getId());
        verify(projectRepository).save(any(ProjectEntity.class));
        verify(projectMemberRepository).save(any(ProjectMemberEntity.class));
    }

    @Test
    void createProject_emptyNameThrows() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("");
        projectDTO.setDescription("");

        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(projectDTO));
    }

    @Test
    void getProjectsByUsername_returnsProjects() {
        String username = "user1";

        given(userRepository.existsByUsername(username)).willReturn(true);

        ProjectEntity p1 = ProjectEntity.builder().name("p1").description("d1").build();
        ReflectionTestUtils.setField(p1, "id", 1L);

        ProjectEntity p2 = ProjectEntity.builder().name("p2").description("d2").build();
        ReflectionTestUtils.setField(p2, "id", 2L);

        given(projectMemberRepository.findProjectsByUsername(username)).willReturn(List.of(p1, p2));

        List<ProjectDTO> result = projectService.getProjectsByUsername(username);

        verify(projectMemberRepository, times(1)).findProjectsByUsername(username);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("p1", result.get(0).getName());
        assertEquals("d1", result.get(0).getDescription());
        assertEquals(2L, result.get(1).getId());
        assertEquals("p2", result.get(1).getName());
        assertEquals("d2", result.get(1).getDescription());
    }

    @Test
    void getProjectsByUsername_missingUserThrows() {
        String username = "nonexistent_user";
        given(userRepository.existsByUsername(username)).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> projectService.getProjectsByUsername(username));
    }

    @Test
    void getProjectsByUsername_noProjectsReturnsEmptyList() {
        String username = "user_no_projects";

        given(userRepository.existsByUsername(username)).willReturn(true);
        given(projectMemberRepository.findProjectsByUsername(username)).willReturn(List.of());

        List<ProjectDTO> result = projectService.getProjectsByUsername(username);

        verify(projectMemberRepository, times(1)).findProjectsByUsername(username);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateProject_ownerCanUpdate() {
        ProjectEntity existing = new ProjectEntity("old name", "old desc", null);
        String requestedUsername = "requester";
        ReflectionTestUtils.setField(existing, "id", 1L);

        given(projectRepository.findById(1L)).willReturn(Optional.of(existing));
        given(projectRepository.save(any(ProjectEntity.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(projectRepository.existsById(1L)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(ProjectRole.OWNER);

        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("new name");
        dto.setDescription("new desc");

        ProjectDTO result = projectService.updateProject(dto, requestedUsername);

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository).save(captor.capture());

        ProjectEntity saved = captor.getValue();
        assertEquals("new name", saved.getName());
        assertEquals("new desc", saved.getDescription());
        assertEquals(1L, result.getId());
        assertEquals("new name", result.getName());
        assertEquals("new desc", result.getDescription());
    }

    @Test
    void updateProject_nonOwnerThrows() {
        String requestedUsername = "non_owner";

        given(projectRepository.existsById(1L)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(ProjectRole.MEMBER);

        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("new name");
        dto.setDescription("new desc");

        assertThrows(IllegalArgumentException.class, () -> projectService.updateProject(dto, requestedUsername));
    }

    @Test
    void updateProject_missingProjectThrows() {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("new name");
        dto.setDescription("new desc");

        assertThrows(IllegalArgumentException.class, () -> projectService.updateProject(dto, "requester"));
    }

    @Test
    void deleteProject_ownerCanDelete() {
        Long projectId = 1L;
        String requestedUsername = "requester";

        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requestedUsername)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(ProjectRole.OWNER);

        projectService.deleteProject(projectId, requestedUsername);

        verify(projectRepository).existsById(projectId);
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void deleteProject_nonOwnerThrows() {
        Long projectId = 1L;
        String requestedUsername = "requester";

        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requestedUsername)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(1L, requestedUsername)).willReturn(ProjectRole.MEMBER);

        assertThrows(IllegalArgumentException.class, () -> projectService.deleteProject(projectId, requestedUsername));
        verify(projectRepository).existsById(projectId);
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteProject_missingProjectThrows() {
        Long projectId = 99L;
        String requestedUsername = "requester";
        given(projectRepository.existsById(projectId)).willReturn(false);

        assertThrows(IllegalArgumentException.class, () -> projectService.deleteProject(projectId, requestedUsername));
    }
}
