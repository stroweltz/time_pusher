package com.leon.timeconsumer.consumer;

import com.leon.timeconsumer.model.TimeEventEntity;
import com.leon.timeconsumer.repository.TimeEventRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://127.0.0.1:0", "port=0"})
@DirtiesContext
@Import(ReliableKafkaConsumerDbIntegrationTest.DatabaseTestConfiguration.class)
class ReliableKafkaConsumerDbIntegrationTest {

    @TestConfiguration
    public static class DatabaseTestConfiguration {

        @Bean
        @ServiceConnection
        public PostgreSQLContainer<?> postgresContainer() {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                    .withDatabaseName("timedb")
                    .withUsername("postgres")
                    .withPassword("password");
            return container;
        }

        @Bean
        public DataSource dataSource(PostgreSQLContainer<?> container) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(container.getJdbcUrl());
            config.setUsername(container.getUsername());
            config.setPassword(container.getPassword());
            config.setDriverClassName(container.getDriverClassName());
            return new HikariDataSource(config);
        }
    }

    @Autowired
    private TimeEventRepository timeEventRepository;

    @Value("${spring.kafka.consumer.topics.time-topic}")
    private String topic;

    private KafkaTemplate<String, String> stringKafkaTemplate;

    @DynamicPropertySource
    static void kafkaProps(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", () -> System.getProperty("spring.embedded.kafka.brokers"));
        registry.add("spring.kafka.consumer.topics.time-topic", () -> "time-records-test");
        registry.add("spring.kafka.listener.ack-mode", () -> "RECORD");
    }

    @BeforeEach
    void setUp() {
        timeEventRepository.deleteAll();
        String brokers = System.getProperty("spring.embedded.kafka.brokers");
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        stringKafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(configs));
    }

    @AfterEach
    void tearDown() {
        if (stringKafkaTemplate != null) {
            stringKafkaTemplate.destroy();
        }
    }

    @Test
    void testConsumeJsonWithTypeHeadersPersistsTimestamp() {
        LocalDateTime ts = LocalDateTime.of(2025, 11, 4, 13, 52, 35);
        String payload = "{\n\t\"timestamp\": \"" + ts + "\"\n}";

        ProducerRecord<String, String> record = new ProducerRecord<>(
                topic,
                ts.toString(),
                payload
        );
        record.headers().add("__TypeId__", "timeEvent".getBytes(StandardCharsets.UTF_8));

        stringKafkaTemplate.send(record);
        stringKafkaTemplate.flush();

        List<TimeEventEntity> saved = waitForEvents(1, 10);
        assertEquals(1, saved.size());
        assertEquals(ts, saved.get(0).getTimestamp());
    }

    private List<TimeEventEntity> waitForEvents(int expectedCount, int maxSeconds) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < maxSeconds * 1000L) {
            List<TimeEventEntity> all = timeEventRepository.findAll();
            if (all.size() >= expectedCount) {
                return all;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return timeEventRepository.findAll();
    }
}