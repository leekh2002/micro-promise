package com.gyuhyuk.micro_promise.data.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPushEvent(
        String ref,
        String before,
        String after,
        boolean created,
        boolean deleted,
        boolean forced,
        Repository repository,
        Pusher pusher,
        List<Commit> commits,
        HeadCommit head_commit
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            Long id,
            String name,
            String full_name,
            String html_url
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pusher(
            String name,
            String email
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Commit(
            String id,
            String message,
            OffsetDateTime timestamp,
            Author author
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HeadCommit(
            String id,
            String message,
            OffsetDateTime timestamp,
            Author author
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(
            String name,
            String email,
            String username
    ) {
    }
}
