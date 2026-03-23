package com.gyuhyuk.micro_promise.data.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubPingEvent(
        String zen,
        Long hook_id,
        Repository repository
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Repository(
            Long id,
            String name,
            String full_name
    ) {
    }
}
