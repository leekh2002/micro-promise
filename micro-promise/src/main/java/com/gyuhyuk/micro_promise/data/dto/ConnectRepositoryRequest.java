package com.gyuhyuk.micro_promise.data.dto;

import jakarta.validation.constraints.NotBlank;

public record ConnectRepositoryRequest(
        @NotBlank(message = "repositoryUrl is required")
        String repositoryUrl
) {
}
