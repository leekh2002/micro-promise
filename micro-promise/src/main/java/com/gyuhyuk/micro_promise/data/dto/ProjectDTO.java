package com.gyuhyuk.micro_promise.data.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectDTO {
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private Long ownerId;
}
