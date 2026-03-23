package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubBranchResponse;
import com.gyuhyuk.micro_promise.data.dto.GitHubCommitResponse;
import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.webhook.CreateWebhookRequest;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubWebhookResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GitHubClientImpl implements GitHubClient {

    private final RestClient restClient = RestClient.create();

    @Override
    public GitHubRepositoryResponse getRepository(String owner, String repo, String accessToken) {
        return restClient.get()
                .uri("https://api.github.com/repos/{owner}/{repo}", owner, repo)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
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

    @Override
    public GithubWebhookResponse createWebhook(String owner, String repo, String accessToken) {
        CreateWebhookRequest request = new CreateWebhookRequest(
                "web",
                true,
                List.of("push", "pull_request", "create", "delete"),
                Map.of(
                        "url", "https://semibiographical-immutably-michaela.ngrok-free.dev/github/webhook",
                        "content_type", "json",
                        "insecure_ssl", "0"
                )
        );

        return restClient.post()
                .uri("https://api.github.com/repos/{owner}/{repo}/hooks", owner, repo)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .body(request)
                .retrieve()
                .body(GithubWebhookResponse.class);
    }

    @Override
    public List<GitHubBranchResponse> getBranches(String owner, String repo, String accessToken) {
        GitHubBranchResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.github.com")
                        .path("/repos/{owner}/{repo}/branches")
                        .queryParam("per_page", 100)
                        .build(owner, repo))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .retrieve()
                .body(GitHubBranchResponse[].class);

        return response != null ? Arrays.asList(response) : List.of();
    }

    @Override
    public List<GitHubCommitResponse> getCommits(String owner, String repo, String branchName, int limit, String accessToken) {
        GitHubCommitResponse[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.github.com")
                        .path("/repos/{owner}/{repo}/commits")
                        .queryParam("sha", branchName)
                        .queryParam("per_page", limit)
                        .build(owner, repo))
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .retrieve()
                .body(GitHubCommitResponse[].class);

        return response != null ? Arrays.asList(response) : List.of();
    }
}
