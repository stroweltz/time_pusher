package com.leon.timeconsumer.repository;


import com.leon.timeconsumer.model.TimeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEventRepository extends JpaRepository<TimeEventEntity, Long> {


}
