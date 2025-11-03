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
public class TimeRecordProducer {

    private final KafkaTemplate<String, TimeEventDTO> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public void sendTimeRecord(TimeEventDTO event) {
        String topic = kafkaProperties.getTopics().getTimeRecords();

        kafkaTemplate.send(topic, event.getRecordId(), event)
                .addCallback(
                        result -> {
                            log.debug("Successfully sent time record to topic {}: {}",
                                    topic, event.getTimestamp());
                        },
                        failure -> {
                            log.error("Failed to send time record to topic {}: {} with error {}",
                                    topic, event.getTimestamp(), failure);
                        }
                );
    }
}
