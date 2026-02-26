package com.gyuhyuk.micro_promise.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepositoryResponse {

    private Long id;
    private String name;
    private String full_name;
    private String description;
    private Boolean isPrivate;
}