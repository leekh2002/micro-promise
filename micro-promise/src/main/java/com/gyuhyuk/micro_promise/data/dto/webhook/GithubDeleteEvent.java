package com.gyuhyuk.micro_promise.data.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubDeleteEvent(
        String ref,
        String ref_type,
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
