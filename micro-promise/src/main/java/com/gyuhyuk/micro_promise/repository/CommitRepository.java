package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.CommitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitRepository extends JpaRepository<CommitEntity, String> {
}
