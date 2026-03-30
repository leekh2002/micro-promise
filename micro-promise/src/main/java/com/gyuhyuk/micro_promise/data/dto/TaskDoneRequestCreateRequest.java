package com.gyuhyuk.micro_promise.data.dto;

// 완료 요청 생성 시 본문으로 받는 DTO다.
public record TaskDoneRequestCreateRequest(
        // 요청자가 남기는 메시지다.
        String message
) {
}
