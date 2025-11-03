package com.leon.timerest.service;

import com.leon.timerest.model.TimeEventEntity;
import com.leon.timerest.repository.TimeEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TimeEventService {

    private final TimeEventRepository timeEventRepository;

    public Page<TimeEventEntity> findAll(Pageable pageable) {
        return timeEventRepository.findAll(pageable);
    }
}
