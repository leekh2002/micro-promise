package com.gyuhyuk.micro_promise.service;

import com.gyuhyuk.micro_promise.data.dto.GitHubRepositoryResponse;

public interface GitHubClient {
    GitHubRepositoryResponse getRepository(String owner, String repo);
}