package com.gyuhyuk.micro_promise.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubCommitResponse(
        String sha,
        CommitDetail commit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitDetail(
            String message,
            CommitAuthor author
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CommitAuthor(
            String name,
            OffsetDateTime date
    ) {
    }
}
