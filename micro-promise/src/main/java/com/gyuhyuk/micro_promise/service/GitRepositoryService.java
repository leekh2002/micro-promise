package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.ProjectDTO;
import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import com.gyuhyuk.micro_promise.data.entity.ProjectRepositoryEntity;
import com.gyuhyuk.micro_promise.repository.GitRepoRepository;
import com.gyuhyuk.micro_promise.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitRepositoryService {
    private final GitRepoRepository gitRepoRepository;
    private final ProjectRepository projectRepository;
    private final GitHubClient gitHubClient;

    public GitRepositoryService(GitRepoRepository gitRepoRepository,
                                ProjectRepository projectRepository,
                                GitHubClient gitHubClient) {
        this.gitRepoRepository = gitRepoRepository;
        this.projectRepository = projectRepository;
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
        // 실제로는 GitHub API 등을 호출하여 저장소 URL을 가져오는 로직이 필요합니다.
        // 여기서는 예시로 고정된 URL을 반환하도록 하겠습니다.


        String[] parsed = parse(repositoryUrl);
        String owner = parsed[0];
        String repo = parsed[1];

        // 🔥 이제 직접 RestClient 호출 안 함
        GitHubRepositoryResponse repositoryResponse =
                gitHubClient.getRepository(owner, repo);

        ProjectEntity projectEntity = projectRepository.findById(project.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        ProjectRepositoryEntity projectRepositoryEntity = ProjectRepositoryEntity.builder()
                .project(projectEntity)
                .repoUrl(repositoryUrl)
                .id(repositoryResponse.getId())
                .repoName(repo)
                .build();



        gitRepoRepository.save(projectRepositoryEntity);

        return repositoryResponse;

    }

}
