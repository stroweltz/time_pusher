package com.leon.timerest.repository;

import com.leon.timerest.model.TimeEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEventRepository extends JpaRepository<TimeEventEntity, Long> {

    Page<TimeEventEntity> findAll(Pageable pageable);
}
