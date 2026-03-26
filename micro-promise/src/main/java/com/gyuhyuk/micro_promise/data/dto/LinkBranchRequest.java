package com.gyuhyuk.micro_promise.data.dto;

import jakarta.validation.constraints.NotNull;

// task에 연결할 branch id를 받는 요청 DTO다.
public record LinkBranchRequest(
        // branch를 식별하기 위한 DB id다. null이면 어느 branch를 연결할지 알 수 없으므로 필수다.
        @NotNull Long branchId
) {
}
