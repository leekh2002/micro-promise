package com.gyuhyuk.micro_promise.data.dto;

import jakarta.validation.constraints.NotBlank;

// task에 연결할 commit sha를 받는 요청 DTO다.
public record LinkCommitRequest(
        // commit을 식별하기 위한 sha다. 빈 문자열이면 대상 commit을 찾을 수 없으므로 필수다.
        @NotBlank String commitSha
) {
}
