package com.gyuhyuk.micro_promise.data.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) //Jackson이 JSON을 객체로 바꿀 때, 내 DTO에 없는 필드는 무시하라
public record GithubCreateEvent(
        String ref,
        String ref_type,
        String master_branch,
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
