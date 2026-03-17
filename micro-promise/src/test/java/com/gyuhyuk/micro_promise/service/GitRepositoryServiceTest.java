package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubWebhookResponse;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.UserEntity;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private GitHubClient gitHubClient;

    @Test
    void createWebhook_usesProvidedAccessToken() {
        String owner = "leekyouhyuk2002-oss";
        String repo = "test";
        String accessToken = "github-access-token";
        GithubWebhookResponse webhookResponse = new GithubWebhookResponse();

        given(gitHubClient.createWebhook(owner, repo, accessToken)).willReturn(webhookResponse);

        GithubWebhookResponse response = gitRepositoryService.createWebhook(owner, repo, accessToken);

        assertNotNull(response);
        verify(gitHubClient).createWebhook(eq(owner), eq(repo), eq(accessToken));   //gitHubClient.createWebhook(...)가 호출될 때 각 인자가 owner, repo, accessToken과 같은 값이었는지 확인
    }

    @Test
    void connectRepository_ownerRequestUsesOwnerGithubToken() {
        Long projectId = 1L;
        String username = "GITHUB 100";
        String repositoryUrl = "https://github.com/owner/repo";
        String accessToken = "github-access-token";

        UserEntity ownerUser = UserEntity.builder()
                .username(username)
                .email("owner@test.com")
                .name("owner")
                .githubAccessToken(accessToken)
                .build();
        ProjectEntity project = ProjectEntity.builder()
                .name("project")
                .description("desc")
                .build();
        ProjectMemberEntity ownerMember = ProjectMemberEntity.builder()
                .project(project)
                .user(ownerUser)
                .role(ProjectRole.OWNER)
                .active(true)
                .build();
        GitHubRepositoryResponse repositoryResponse =
                new GitHubRepositoryResponse(123L, "repo", "owner/repo", null, false);

        given(projectRepository.existsById(projectId)).willReturn(true);
        given(projectMemberRepository.existsByProjectIdAndUserUsername(projectId, username)).willReturn(true);
        given(projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, username)).willReturn(ProjectRole.OWNER);
        given(projectMemberRepository.findByProjectIdAndRoleAndActiveTrue(projectId, ProjectRole.OWNER))
                .willReturn(Optional.of(ownerMember));
        given(projectRepository.findById(projectId)).willReturn(Optional.of(project));
        given(gitHubClient.getRepository("owner", "repo", accessToken)).willReturn(repositoryResponse);
        given(gitHubClient.createWebhook("owner", "repo", accessToken)).willReturn(new GithubWebhookResponse());

        GitHubRepositoryResponse response = gitRepositoryService.connectRepository(projectId, repositoryUrl, username);

        assertNotNull(response);
        verify(gitRepoRepository).save(any(ProjectRepositoryEntity.class));
        verify(gitHubClient).createWebhook(eq("owner"), eq("repo"), eq(accessToken));
    }
}
