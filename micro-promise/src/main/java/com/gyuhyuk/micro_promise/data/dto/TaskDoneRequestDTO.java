package com.gyuhyuk.micro_promise.data.dto;

import com.gyuhyuk.micro_promise.data.entity.TaskDoneRequestEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 완료 요청 응답에 사용하는 DTO다.
@Getter
@Builder
public class TaskDoneRequestDTO {
    // 완료 요청 ID다.
    private Long id;
    // 요청 대상 task ID다.
    private Long taskId;
    // 요청을 생성한 사용자 username이다.
    private String requesterUsername;
    // 승인/거절 권한을 가진 대상 owner username이다.
    private String targetTaskOwnerUsername;
    // 완료 요청 상태 문자열이다.
    private String status;
    // 요청 메시지다.
    private String message;
    // 처리 시각이다.
    private LocalDateTime decidedAt;

    // 엔티티를 응답 DTO로 변환한다.
    public static TaskDoneRequestDTO fromEntity(TaskDoneRequestEntity entity) {
        // builder를 사용해 필드를 순서대로 채운다.
        return TaskDoneRequestDTO.builder()
                // 완료 요청 ID를 복사한다.
                .id(entity.getId())
                // 연결된 task ID를 복사한다.
                .taskId(entity.getTask().getId())
                // 요청자 username을 복사한다.
                .requesterUsername(entity.getRequester().getUser().getUsername())
                // 대상 owner username을 복사한다.
                .targetTaskOwnerUsername(entity.getTargetTaskOwner().getUser().getUsername())
                // enum 상태를 문자열로 변환해 복사한다.
                .status(entity.getStatus().name())
                // 메시지를 복사한다.
                .message(entity.getMessage())
                // 처리 시각을 복사한다.
                .decidedAt(entity.getDecidedAt())
                // DTO 생성을 마무리한다.
                .build();
    }
}
