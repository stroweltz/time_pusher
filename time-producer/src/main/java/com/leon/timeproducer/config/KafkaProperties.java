package com.leon.timeproducer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {

    private Topics topics = new Topics();
    private Producer producer = new Producer();

    @Data
    public static class Topics {
        private String timeRecords;
        private String timeRecordsDlt;
    }

    @Data
    public static class Producer {
        private String idPrefix;
    }
}


