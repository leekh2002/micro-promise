package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceTest {
    @InjectMocks
    private ProjectMemberService projectMemberService;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void deleteProjectMember_테스트() {
        // given
        Long projectId = 1L;
        String username = "user1";
        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username)).willReturn(true);

        // when
        projectMemberService.deleteProjectMember(projectId, username);

        // then
        assertDoesNotThrow(() -> projectMemberRepository.deleteByProjectIdAndUserUsername(projectId, username));
    }

}