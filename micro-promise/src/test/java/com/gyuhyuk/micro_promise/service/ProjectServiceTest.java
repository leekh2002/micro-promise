package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    void create_테스트() {
        //given
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("프로젝트1");
        projectDTO.setDescription("설명1");
        projectDTO.setCreatedAt(null);
        projectDTO.setOwnerId(1L);

        ProjectEntity projectEntity = new ProjectEntity("프로젝트1", "설명1", null);
        ReflectionTestUtils.setField(projectEntity, "id", 1L);
        given(projectRepository.save(any(ProjectEntity.class))).willReturn(projectEntity);

        //when
        projectService.createProject(projectDTO);

        //then
        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void create_제목빈값_테스트() {
        //given
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("");
        projectDTO.setDescription("");
        projectDTO.setCreatedAt(null);
        projectDTO.setOwnerId(2L);

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject(projectDTO);
        });
    }

    @Test
    void createProject_정상적으로_저장된다() {
        // given
        ProjectDTO dto = new ProjectDTO();
        dto.setName("abcd");
        dto.setDescription("desc");

        // save가 호출되면 id가 있는 엔티티를 반환하도록 스텁
        given(projectRepository.save(any(ProjectEntity.class)))
                .willAnswer(invocation -> {
                    ProjectEntity e = invocation.getArgument(0);
                    ReflectionTestUtils.setField(e, "id", 1L);
                    return e;
                });

        // when
        projectService.createProject(dto);

        // then: save가 호출됐는지 + 저장 엔티티 값 검증
        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        verify(projectRepository, times(1)).save(captor.capture());

        ProjectEntity saved = captor.getValue();
        assertEquals("abcd", saved.getName());
        assertEquals("desc", saved.getDescription());
    }

    @Test
    void getProjectsByUsername_테스트1() {
        // given
        String username = "user1";

        given(userRepository.existsByUsername(username)).willReturn(true);

        ProjectEntity p1 = ProjectEntity.builder()
                .name("p1")
                .description("d1")
                .build();
        ReflectionTestUtils.setField(p1, "id", 1L);

        ProjectEntity p2 = ProjectEntity.builder()
                .name("p2")
                .description("d2")
                .build();
        ReflectionTestUtils.setField(p2, "id", 2L);

        given(projectMemberRepository.findProjectsByUsername(username))
                .willReturn(List.of(p1, p2));

        // when
        List<ProjectDTO> result =
                projectService.getProjectsByUsername(username);

        // then: 레포 호출 검증
        verify(projectMemberRepository, times(1))
                .findProjectsByUsername(username);

        // then: 변환 결과 검증
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("p1", result.get(0).getName());
        assertEquals("d1", result.get(0).getDescription());

        assertEquals(2L, result.get(1).getId());
        assertEquals("p2", result.get(1).getName());
        assertEquals("d2", result.get(1).getDescription());
    }

    @Test
    void getProjectsByUsername_존재하지_않는_사용자_테스트() {
        // given
        String username = "nonexistent_user";

        given(userRepository.existsByUsername(username)).willReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.getProjectsByUsername(username);
        });
    }

    @Test
    void getProjectsByUsername_프로젝트_없음_테스트() {
        // given
        String username = "user_no_projects";

        given(userRepository.existsByUsername(username)).willReturn(true);
        given(projectMemberRepository.findProjectsByUsername(username))
                .willReturn(List.of());

        // when
        List<ProjectDTO> result =
                projectService.getProjectsByUsername(username);

        // then
        verify(projectMemberRepository, times(1))
                .findProjectsByUsername(username);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProject_테스트() {
        // given
        ProjectEntity existing = new ProjectEntity("old name", "old desc", null);

        // id는 리플렉션으로 세팅 (setter 없을 때 정석)
        ReflectionTestUtils.setField(existing, "id", 1L);

        given(projectRepository.findById(1L))
                .willReturn(Optional.of(existing));

        given(projectRepository.save(any(ProjectEntity.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(projectRepository.existsById(1L))
                .willReturn(true);

        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("new name");
        dto.setDescription("new desc");

        // when
        ProjectDTO result = projectService.updateProject(dto);

        // then
        ArgumentCaptor<ProjectEntity> captor =
                ArgumentCaptor.forClass(ProjectEntity.class);

        verify(projectRepository).findById(1L);
        verify(projectRepository).save(captor.capture());

        ProjectEntity saved = captor.getValue();
        assertEquals("new name", saved.getName());
        assertEquals("new desc", saved.getDescription());

        assertEquals(1L, result.getId());
        assertEquals("new name", result.getName());
        assertEquals("new desc", result.getDescription());
    }

    @Test
    void updateProject_no_exist_project() {
        // given
        ProjectEntity existing = new ProjectEntity("old name", "old desc", null);

        // id는 리플렉션으로 세팅 (setter 없을 때 정석)
        ReflectionTestUtils.setField(existing, "id", 2L);

        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("new name");
        dto.setDescription("new desc");

        // when & then
        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.updateProject(dto)
        );

    }

    @Test
    void deleteProject_테스트() {
        // given
        Long projectId = 1L;
        given(projectRepository.existsById(projectId)).willReturn(true);

        // when
        projectService.deleteProject(projectId);

        // then
        verify(projectRepository).existsById(projectId);
        verify(projectRepository).deleteById(projectId);
    }

    @Test
    void deleteProject_존재하지_않는_프로젝트_테스트() {
        // given
        Long projectId = 99L;
        given(projectRepository.existsById(projectId)).willReturn(false);
        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.deleteProject(projectId);
        });
    }


}