package com.leon.timeproducer.producer;

import com.leon.timeproducer.config.KafkaProperties;
import com.leon.timeproducer.model.TimeEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;


import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;


@ExtendWith(MockitoExtension.class)
class TimeEventProducerTest {

    @Mock
    private KafkaTemplate<String, TimeEventDTO> kafkaTemplate;

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private KafkaProperties.Topics topics;

    @InjectMocks
    private TimeEventProducer timeEventProducer;

    @BeforeEach
    void setUp() {
        when(kafkaProperties.getTopics()).thenReturn(topics);
        when(topics.getTimeRecords()).thenReturn("test-topic");
    }

    @Test
    void testSendTimeEventSuccess() {
        TimeEventDTO event = new TimeEventDTO();
        LocalDateTime timestamp = LocalDateTime.now();
        event.setTimestamp(timestamp);

        CompletableFuture<SendResult<String, TimeEventDTO>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), anyString(), any(TimeEventDTO.class)))
                .thenReturn(future);

        timeEventProducer.sendTimeEvent(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<TimeEventDTO> eventCaptor = ArgumentCaptor.forClass(TimeEventDTO.class);

        verify(kafkaTemplate, times(1)).send(
                topicCaptor.capture(),
                keyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals("test-topic", topicCaptor.getValue());
        assertEquals(timestamp.toString(), keyCaptor.getValue());
        assertEquals(event, eventCaptor.getValue());
    }

    @Test
    void testSendTimeEventWithNullTimestamp() {
        TimeEventDTO event = new TimeEventDTO();
        event.setTimestamp(null);

        CompletableFuture<SendResult<String, TimeEventDTO>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));

        when(kafkaTemplate.send(anyString(), isNull(), any(TimeEventDTO.class)))
                .thenReturn(future);

        timeEventProducer.sendTimeEvent(event);

        verify(kafkaTemplate, times(1)).send(eq("test-topic"), isNull(), eq(event));
    }

    @Test
    void testSendTimeEventFailure() {
        TimeEventDTO event = new TimeEventDTO();
        LocalDateTime timestamp = LocalDateTime.now();
        event.setTimestamp(timestamp);

        CompletableFuture<SendResult<String, TimeEventDTO>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any(TimeEventDTO.class)))
                .thenReturn(future);

        timeEventProducer.sendTimeEvent(event);

        verify(kafkaTemplate, times(1)).send(eq("test-topic"), eq(timestamp.toString()), eq(event));
    }

    @Test
    void testSendTimeEventNullEventThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> timeEventProducer.sendTimeEvent(null));
    }

    @Test
    void testSendTimeEventKafkaTemplateThrowsSynchronouslyPropagates() {
        TimeEventDTO event = new TimeEventDTO();
        LocalDateTime timestamp = LocalDateTime.now();
        event.setTimestamp(timestamp);

        when(kafkaTemplate.send(anyString(), anyString(), any(TimeEventDTO.class)))
                .thenThrow(new RuntimeException("sync send error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> timeEventProducer.sendTimeEvent(event));
        assertEquals("sync send error", ex.getMessage());
        verify(kafkaTemplate, times(1)).send(eq("test-topic"), eq(timestamp.toString()), eq(event));
    }

    @Test
    void testSendTimeEventMissingTopicConfigurationThrows() {
        when(topics.getTimeRecords()).thenReturn(null);
        TimeEventDTO event = new TimeEventDTO();
        event.setTimestamp(LocalDateTime.now());

        assertThrows(Exception.class, () -> timeEventProducer.sendTimeEvent(event));
    }
}

