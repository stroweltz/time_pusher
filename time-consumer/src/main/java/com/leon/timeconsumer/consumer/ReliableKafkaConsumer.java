package com.leon.timeconsumer.consumer;

import com.leon.timeconsumer.aspect.RetryPolicy;
import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.service.TimeEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ReliableKafkaConsumer {

    private final TimeEventService timeEventService;

    @KafkaListener(topics = "${spring.kafka.consumer.topics.time-topic}", concurrency = "${spring.kafka.consumer.concurrency}")
    @RetryPolicy(
            maxAttempts = 1,
            infiniteRetryExceptions = {
                    DataAccessResourceFailureException.class,
                    CannotGetJdbcConnectionException.class},
            retryExceptions = {DataIntegrityViolationException.class}
    )
    public void consumeMessage(@Payload TimeEventDTO message) {
        timeEventService.createEvent(message);
    }
}