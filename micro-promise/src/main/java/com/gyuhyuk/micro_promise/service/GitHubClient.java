package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;
import com.gyuhyuk.micro_promise.data.dto.webhook.GithubWebhookResponse;

public interface GitHubClient {
    GitHubRepositoryResponse getRepository(String owner, String repo, String accessToken);

    GithubWebhookResponse createWebhook(String owner, String repo, String accessToken);
}
