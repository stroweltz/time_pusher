package com.leon.timeproducer.producer;

import com.leon.timeproducer.model.TimeEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TimeEventSchedulerTest {

    @Mock
    private TimeEventProducer timeEventProducer;

    @InjectMocks
    private TimeEventScheduler timeEventScheduler;

    @BeforeEach
    void setUp() {
        doNothing().when(timeEventProducer).sendTimeEvent(any(TimeEventDTO.class));
    }

    @Test
    void testProduceTimeEventCreatesTimeEventWithRoundedTimestamp() {
        timeEventScheduler.produceTimeEvent();

        ArgumentCaptor<TimeEventDTO> eventCaptor = ArgumentCaptor.forClass(TimeEventDTO.class);
        verify(timeEventProducer, times(1)).sendTimeEvent(eventCaptor.capture());

        TimeEventDTO capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertNotNull(capturedEvent.getTimestamp());
        
        LocalDateTime timestamp = capturedEvent.getTimestamp();
        assertEquals(0, timestamp.getNano());
    }

    @Test
    void testProduceTimeEventTimeEventIsSent() {
        timeEventScheduler.produceTimeEvent();

        verify(timeEventProducer, times(1)).sendTimeEvent(any(TimeEventDTO.class));
    }

    @Test
    void testProduceTimeEventTimestampIsRoundedToSeconds() {
        timeEventScheduler.produceTimeEvent();

        ArgumentCaptor<TimeEventDTO> eventCaptor = ArgumentCaptor.forClass(TimeEventDTO.class);
        verify(timeEventProducer).sendTimeEvent(eventCaptor.capture());

        TimeEventDTO event = eventCaptor.getValue();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime roundedNow = now.truncatedTo(ChronoUnit.SECONDS);

        assertTrue(Math.abs(event.getTimestamp().getSecond() - roundedNow.getSecond()) <= 1);
    }
}

