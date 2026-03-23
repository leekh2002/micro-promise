package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubBranchResponse;
import com.gyuhyuk.micro_promise.data.dto.GitHubCommitResponse;
import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubWebhookResponse;

import java.util.List;

public interface GitHubClient {
    GitHubRepositoryResponse getRepository(String owner, String repo, String accessToken);

    GithubWebhookResponse createWebhook(String owner, String repo, String accessToken);

    List<GitHubBranchResponse> getBranches(String owner, String repo, String accessToken);

    List<GitHubCommitResponse> getCommits(String owner, String repo, String branchName, int limit, String accessToken);
}
