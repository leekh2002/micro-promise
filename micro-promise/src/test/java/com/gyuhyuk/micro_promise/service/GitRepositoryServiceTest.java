package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GitRepositoryServiceTest {
    @InjectMocks
    private GitRepositoryService gitRepositoryService;

    @Mock
    private GitRepoRepository gitRepoRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GitHubClient gitHubClient;

    @Test
    void connectGitRepository_테스트_올바른_URL() {
        // given
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(1L);
        projectDTO.setName("프로젝트1");
        projectDTO.setDescription("설명1");
        projectDTO.setCreatedAt(null);
        projectDTO.setOwnerId(1L);
        String gitRepoUrl = "https://github.com/owner/repo";
        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("프로젝트1")
                .description("설명1")
                .build();

        GitHubRepositoryResponse response =
                new GitHubRepositoryResponse(123L, "repo", "owner/repo", null, false);

        given(projectRepository.findById(any(Long.class))).willReturn(java.util.Optional.of(projectEntity));

        given(gitHubClient.getRepository("owner", "repo"))
                .willReturn(response);

        // when
        GitHubRepositoryResponse repositoryResponse = gitRepositoryService.connectRepository(projectDTO, gitRepoUrl);

        // then
        // gitRepoRepository의 save() 메서드가 호출되었는지 검증
        verify(gitRepoRepository).save(any(ProjectRepositoryEntity.class));
        assertEquals("repo", repositoryResponse.getName());
    }

    @Test
    void connectGitRepository_테스트_잘못된_URL() {
        // given
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(1L);
        projectDTO.setName("프로젝트1");
        projectDTO.setDescription("설명1");
        projectDTO.setCreatedAt(null);
        projectDTO.setOwnerId(1L);
        String gitRepoUrl = "https://github.com/leekh2002/private-tes";

        ProjectEntity projectEntity = ProjectEntity.builder()
                .name("프로젝트1")
                .description("설명1")
                .build();

        given(gitHubClient.getRepository("leekh2002", "private-tes"))
                .willThrow(new IllegalArgumentException());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            gitRepositoryService.connectRepository(projectDTO, gitRepoUrl);
        });
    }
}