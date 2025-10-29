package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_item",
        indexes = {
                @Index(name = "idx_checklist_project", columnList = "project_id")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChecklistItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean checked;

    @Column(nullable = false)
    private int orderNo;
}