package com.gyuhyuk.micro_promise.data.dto;

// 완료 요청 승인/거절 시 본문으로 받는 DTO다.
public record TaskDoneRequestDecisionRequest(
        // true면 승인, false면 거절이다.
        boolean approve
) {
}
