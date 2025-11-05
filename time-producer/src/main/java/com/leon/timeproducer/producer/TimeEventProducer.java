package com.leon.timeproducer.producer;

import com.leon.timeproducer.config.KafkaProperties;
import com.leon.timeproducer.model.TimeEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeEventProducer {

    private final KafkaTemplate<String, TimeEventDTO> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void sendTimeEvent(TimeEventDTO event) {
        String topic = kafkaProperties.getTopics().getTimeRecords();

        String key = event.getTimestamp() != null ? event.getTimestamp().toString() : null;
        kafkaTemplate.send(topic, key, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.debug("Successfully sent time record to topic {}: {}", topic, event.getTimestamp());
                    } else {
                        log.error("Failed to send time record to topic {}: {} with error {}", topic, event.getTimestamp(), ex.getMessage());
                    }
                });
    }
}
