package com.gyuhyuk.micro_promise.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubBranchResponse(
        String name
) {
}
