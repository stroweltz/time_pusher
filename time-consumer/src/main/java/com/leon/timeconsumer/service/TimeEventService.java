package com.leon.timeconsumer.service;

import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.model.TimeEventEntity;
import com.leon.timeconsumer.repository.TimeEventRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class TimeEventService {

    private final TimeEventRepository timeEventRepository;

    @Transactional
    public TimeEventEntity createEvent(TimeEventDTO timeEventDTO) {
        TimeEventEntity event = new TimeEventEntity();
        event.setCreatedAt(timeEventDTO.getTimestamp());
        return timeEventRepository.save(event);
    }
}
