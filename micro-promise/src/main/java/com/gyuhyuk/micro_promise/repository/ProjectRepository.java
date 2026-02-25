package com.gyuhyuk.micro_promise.repository;

import com.gyuhyuk.micro_promise.data.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

}
