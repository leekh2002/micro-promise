package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    private LocalDateTime createdDate;

    @Builder
    public ProjectEntity(String name, String description, LocalDateTime createdDate) {
        this.name = name;
        this.description = description;
        this.createdDate = createdDate;
    }

    public void updateProjectInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }
}