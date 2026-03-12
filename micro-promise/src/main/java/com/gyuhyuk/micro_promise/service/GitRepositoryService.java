package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubWebhookResponse;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectMemberEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRole;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectMemberRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class GitRepositoryService {
    private final GitRepoRepository gitRepoRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final GitHubClient gitHubClient;

    public GitRepositoryService(GitRepoRepository gitRepoRepository,
                                ProjectRepository projectRepository,
                                ProjectMemberRepository projectMemberRepository,
                                GitHubClient gitHubClient) {
        this.gitRepoRepository = gitRepoRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.gitHubClient = gitHubClient;
    }

    public static String[] parse(String repositoryUrl) {
        if (!repositoryUrl.startsWith("https://github.com/")) {
            throw new IllegalArgumentException("Invalid GitHub URL");
        }

        String path = repositoryUrl
                .replace("https://github.com/", "")
                .replace(".git", "");

        String[] parts = path.split("/");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid GitHub repository format");
        }

        return new String[]{parts[0], parts[1]};
    }

    public GitHubRepositoryResponse connectRepository(ProjectDTO project, String repositoryUrl) {
        return connectRepository(project.getId(), repositoryUrl, null);
    }

    public GitHubRepositoryResponse connectRepository(Long projectId, String repositoryUrl, String requestedUsername) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new IllegalArgumentException("Project does not exist");
        }
        if (requestedUsername != null) {
            if (!projectMemberRepository.existsByProjectIdAndUserUsername(projectId, requestedUsername)) {
                throw new IllegalArgumentException("User is not a member of the project");
            }
            if (projectMemberRepository.findRoleByProjectIdAndUserUsername(projectId, requestedUsername) != ProjectRole.OWNER) {
                throw new IllegalArgumentException("Only project owners can connect a repository");
            }
        }

        String[] parsed = parse(repositoryUrl);
        String owner = parsed[0];
        String repo = parsed[1];

        ProjectMemberEntity ownerMember = projectMemberRepository.findByProjectIdAndRoleAndActiveTrue(projectId, ProjectRole.OWNER)
                .orElseThrow(() -> new IllegalArgumentException("Project owner not found"));

        String ownerGithubToken = ownerMember.getUser().getGithubAccessToken();
        if (ownerGithubToken == null || ownerGithubToken.isBlank()) {
            throw new IllegalArgumentException("Project owner must log in with GitHub before connecting a repository");
        }

        GitHubRepositoryResponse repositoryResponse = gitHubClient.getRepository(owner, repo, ownerGithubToken);

        ProjectEntity projectEntity = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectRepositoryEntity projectRepositoryEntity = ProjectRepositoryEntity.builder()
                .project(projectEntity)
                .repoUrl(repositoryUrl)
                .id(repositoryResponse.getId())
                .repoName(repo)
                .build();

        gitRepoRepository.save(projectRepositoryEntity);
        createWebhook(owner, repo, ownerGithubToken);

        return repositoryResponse;
    }

    public GithubWebhookResponse createWebhook(String owner, String repo, String accessToken) {
        return gitHubClient.createWebhook(owner, repo, accessToken);
    }
}
