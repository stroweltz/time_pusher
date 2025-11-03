package com.leon.timeconsumer.consumer;

import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.service.LazyDatabaseConnectionMonitor;
import com.leon.timeconsumer.service.TimeEventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class ReliableKafkaConsumer {

    private final TimeEventService timeEventService;
    private final LazyDatabaseConnectionMonitor dbMonitor;

    @KafkaListener(topics = "${kafka.topics.your-topic}", concurrency = "${kafka.concurrency}")
    public void consumeMessage(TimeEventDTO message, Acknowledgment ack) {
        boolean processed = false;
        int totalAttempts = 0;

        while (!processed) {
            totalAttempts++;

            try {
                if (!dbMonitor.isDatabaseAvailable()) {
                    log.warn("Database not connected, attempt {}. Waiting...", totalAttempts);
                    dbMonitor.waitForDatabaseRecovery();
                    continue;
                }

                processMessageInTransaction(message);
                ack.acknowledge();
                processed = true;
                log.info("Message processed successfully on attempt {}", totalAttempts);

            } catch (DataAccessException e) {
                log.warn("Database error on attempt {}", totalAttempts, e);
                waitWithBackoff(totalAttempts);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for database", e);
            }
        }
    }


    private void waitWithBackoff(int attempt) {
        try {
            long backoff = Math.min(1000 * (long) Math.pow(2, attempt), 30000);
            Thread.sleep(backoff);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected void processMessageInTransaction(TimeEventDTO message) {
        timeEventService.createEvent(message);
    }
}