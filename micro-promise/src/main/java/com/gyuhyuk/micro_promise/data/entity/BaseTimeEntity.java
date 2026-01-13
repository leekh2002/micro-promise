package com.gyuhyuk.micro_promise.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass   //부모 클래스의 필드를 자식 엔티티의 컬럼으로 “그대로 복사”해주는 JPA 어노테이션
@Getter
public abstract class BaseTimeEntity {

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate  //엔티티가 UPDATE 되기 직전에 자동으로 실행되는 메서드를 지정하는 어노테이션
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}