package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.AcceptProjectInviteRequest;
import com.gyuhyuk.micro_promise.data.dto.UserDTO;
import com.gyuhyuk.micro_promise.data.entity.*;
import com.gyuhyuk.micro_promise.repository.ProjectInviteCodeRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import com.gyuhyuk.micro_promise.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ProjectInviteServiceTest {
    @InjectMocks
    private ProjectInviteService projectInviteService;

    @Mock
    private ProjectInviteCodeRepository projectInviteCodeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void generateInviteCode_테스트() {
        //given
        Long projectId = 1L;
        String regex = "^PRJ-1-[0-9A-HJ-NP-TV-Z]{12}$";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("Test Project")
                .build();
        ReflectionTestUtils.setField(projectEntity, "id", projectId);

        ProjectInviteCodeEntity inviteCodeEntity = ProjectInviteCodeEntity.builder()
                .code("PRJ-1-K7D93FQW")
                .project(projectEntity)
                .build();
        ReflectionTestUtils.setField(inviteCodeEntity, "id", 1L);

        given(projectInviteCodeRepository.save(any(ProjectInviteCodeEntity.class)))
                .willReturn(inviteCodeEntity);

        given(projectRepository.findById(projectId))
                .willReturn(java.util.Optional.of(projectEntity));

        // when
        String inviteCode = projectInviteService.generateInviteCode(projectId);

        // then
        verify(projectInviteCodeRepository).save(any(ProjectInviteCodeEntity.class));
        assertTrue(inviteCode.matches(regex));
    }

    @Test
    void generateInviteCode_유효하지_않은_프로젝트ID_테스트() {
        // given
        Long invalidProjectId = 999L;

        given(projectRepository.findById(invalidProjectId))
                .willReturn(java.util.Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectInviteService.generateInviteCode(invalidProjectId);
        });

        assertEquals("Invalid project ID", exception.getMessage());
    }

    @Test
    void updateInviteCode_테스트() {
        // given
        Long projectId = 12L;

        ProjectEntity project = ProjectEntity.builder()
                .name("Update Invite Code Project")
                .description("desc")
                .build();
        ReflectionTestUtils.setField(project, "id", projectId);

        given(projectRepository.findById(projectId))
                .willReturn(Optional.of(project));

        given(projectInviteCodeRepository.save(any(ProjectInviteCodeEntity.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        String newCode = projectInviteService.updateInviteCode(projectId);

        // then
        verify(projectRepository).findById(projectId);


        // then 2: 새 코드 저장
        ArgumentCaptor<ProjectInviteCodeEntity> captor =
                ArgumentCaptor.forClass(ProjectInviteCodeEntity.class);
        verify(projectInviteCodeRepository).save(captor.capture());

        ProjectInviteCodeEntity saved = captor.getValue();
        assertEquals(project, saved.getProject());

        // then 4️⃣: 코드 패턴 검증
        String regex = "^PRJ-" + projectId + "-[0-9A-HJ-NP-TV-Z]{12}$";
        assertTrue(newCode.matches(regex), "invite code pattern mismatch");
        assertEquals(newCode, saved.getCode());

        verifyNoMoreInteractions(projectInviteCodeRepository);
    }

    @Test
    void updateInviteCode_유효하지_않은_프로젝트ID_테스트() {
        // given
        Long invalidProjectId = 888L;

        given(projectRepository.findById(invalidProjectId))
                .willReturn(Optional.empty());

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            projectInviteService.updateInviteCode(invalidProjectId);
        });

        assertEquals("Invalid project ID", exception.getMessage());
    }

    @Test
    void acceptProjectInvite_테스트() {
        // given
        UserDTO user = new UserDTO();
        UserEntity userEntity = UserEntity.builder()
                .name("Test User")
                .username("testuser")
                .role(UserRole.ROLE_USER)
                .build();

        ReflectionTestUtils.setField(userEntity, "id", 1L);

        user.setName("Test User");
        user.setUsername("testuser");
        user.setRole("USER");

        String inviteCode = "PRJ-1-ABCDEFGHJKLMN";
        String invalidCode = "INVALID-CODE";

        ProjectEntity project = ProjectEntity.builder()
                .name("Test Project")
                .build();
        ReflectionTestUtils.setField(project, "id", 1L);

        ProjectInviteCodeEntity inviteCodeEntity = ProjectInviteCodeEntity.builder()
                .code(inviteCode)
                .revoked(false)
                .project(project)
                .build();

        given(userRepository.findByUsername(user.getUsername()))
                .willReturn(userEntity);

        given(projectInviteCodeRepository.findByCode(invalidCode))
                .willReturn(Optional.empty());

        given(projectInviteCodeRepository.findByCode(inviteCode))
                .willReturn(Optional.of(inviteCodeEntity));

        given(projectMemberRepository.save(any(ProjectMemberEntity.class)))
                .willAnswer(inv -> {
                    Object arg = inv.getArgument(0);
                    ReflectionTestUtils.setField(arg, "id", 1L);
                    return arg;
                });

        // when & then
        assertDoesNotThrow(() ->
                projectInviteService.acceptProjectInvite(user, inviteCode)
        );
        verify(projectInviteCodeRepository).findByCode(inviteCode);

        assertThrows(IllegalArgumentException.class,
                () -> projectInviteService.acceptProjectInvite(user, invalidCode));

        verify(projectMemberRepository).save(any(ProjectMemberEntity.class));
        assertEquals(1L, projectInviteService.acceptProjectInvite(user, inviteCode).getId());
    }

}