package com.leon.timeconsumer.service;

import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.model.TimeEventEntity;
import com.leon.timeconsumer.repository.TimeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class TimeEventServiceTest {

    @Mock
    private TimeEventRepository timeEventRepository;

    @InjectMocks
    private TimeEventService timeEventService;

    private TimeEventDTO testDTO;
    private TimeEventEntity savedEntity;

    @BeforeEach
    void setUp() {
        testDTO = new TimeEventDTO();
        testDTO.setTimestamp(LocalDateTime.now());

        savedEntity = new TimeEventEntity();
        savedEntity.setId(1L);
        savedEntity.setTimestamp(testDTO.getTimestamp());
    }

    @Test
    void testCreateEventSuccess() {
        when(timeEventRepository.save(any(TimeEventEntity.class))).thenReturn(savedEntity);

        TimeEventEntity result = timeEventService.createEvent(testDTO);

        assertNotNull(result);
        assertEquals(savedEntity.getId(), result.getId());
        assertEquals(testDTO.getTimestamp(), result.getTimestamp());

        ArgumentCaptor<TimeEventEntity> entityCaptor = ArgumentCaptor.forClass(TimeEventEntity.class);
        verify(timeEventRepository, times(1)).save(entityCaptor.capture());

        TimeEventEntity capturedEntity = entityCaptor.getValue();
        assertEquals(testDTO.getTimestamp(), capturedEntity.getTimestamp());
    }

    @Test
    void testCreateEventMapsTimestampCorrectly() {
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 5, 10, 30, 0);
        testDTO.setTimestamp(timestamp);
        savedEntity.setTimestamp(timestamp);
        when(timeEventRepository.save(any(TimeEventEntity.class))).thenReturn(savedEntity);

        TimeEventEntity result = timeEventService.createEvent(testDTO);

        ArgumentCaptor<TimeEventEntity> entityCaptor = ArgumentCaptor.forClass(TimeEventEntity.class);
        verify(timeEventRepository).save(entityCaptor.capture());

        TimeEventEntity capturedEntity = entityCaptor.getValue();
        assertEquals(timestamp, capturedEntity.getTimestamp());
        assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    void testCreateEventWithNullTimestamp() {
        testDTO.setTimestamp(null);
        savedEntity.setTimestamp(null);
        when(timeEventRepository.save(any(TimeEventEntity.class))).thenReturn(savedEntity);

        TimeEventEntity result = timeEventService.createEvent(testDTO);

        assertNotNull(result);
        assertNull(result.getTimestamp());

        ArgumentCaptor<TimeEventEntity> entityCaptor = ArgumentCaptor.forClass(TimeEventEntity.class);
        verify(timeEventRepository).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getTimestamp());
    }
}