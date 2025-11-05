package com.leon.timeconsumer.consumer;

import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.model.TimeEventEntity;
import com.leon.timeconsumer.service.TimeEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReliableKafkaConsumerTest {

    @Mock
    private TimeEventService timeEventService;

    @InjectMocks
    private ReliableKafkaConsumer consumer;

    private TimeEventDTO testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new TimeEventDTO();
        testEvent.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testConsumeMessageDelegatesToService() {
        when(timeEventService.createEvent(any(TimeEventDTO.class))).thenReturn(new TimeEventEntity());

        consumer.consumeMessage(testEvent);

        verify(timeEventService, times(1)).createEvent(testEvent);
    }

    @Test
    void testConsumeMessagePropagatesDataAccessExceptionWithoutAspect() {
        when(timeEventService.createEvent(any(TimeEventDTO.class)))
                .thenThrow(new DataAccessException("Database error") {});

        assertThrows(DataAccessException.class, () -> consumer.consumeMessage(testEvent));
        verify(timeEventService, times(1)).createEvent(testEvent);
    }
}