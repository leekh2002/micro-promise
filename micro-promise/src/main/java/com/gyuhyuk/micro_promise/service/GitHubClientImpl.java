package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GitHubClientImpl implements GitHubClient {

    @Value("${spring.github.token}")
    private String token;

    private final RestClient restClient = RestClient.create();

    @Override
    public GitHubRepositoryResponse getRepository(String owner, String repo) {

        return restClient.get()
                .uri("https://api.github.com/repos/{owner}/{repo}", owner, repo)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + token)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        (req, res) -> {
                            throw new IllegalArgumentException("Repository not found");
                        }
                )
                .body(GitHubRepositoryResponse.class);
    }
}