package com.leon.timeconsumer.service;

import com.leon.timeconsumer.model.TimeEventDTO;
import com.leon.timeconsumer.model.TimeEventEntity;
import com.leon.timeconsumer.repository.TimeEventRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext
@Import(TimeEventServiceDbIntegrationTest.DatabaseTestConfiguration.class)
class TimeEventServiceDbIntegrationTest {

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

    @Autowired
    private TimeEventService timeEventService;

    @Test
    void testCreateEventPersistsEntity() {
        TimeEventDTO dto = new TimeEventDTO();
        LocalDateTime ts = LocalDateTime.now().withNano(0).plusSeconds(2);
        dto.setTimestamp(ts);

        TimeEventEntity saved = timeEventService.createEvent(dto);

        assertNotNull(saved.getId());
        assertEquals(ts, saved.getTimestamp());

        TimeEventEntity fromDb = timeEventRepository.findById(saved.getId()).orElseThrow();
        assertEquals(ts, fromDb.getTimestamp());
    }

    @Test
    void testUniqueTimestampConstraintThrowsOnDuplicate() {
        LocalDateTime ts = LocalDateTime.now().withNano(0);
        TimeEventDTO dto1 = new TimeEventDTO();
        dto1.setTimestamp(ts);
        TimeEventDTO dto2 = new TimeEventDTO();
        dto2.setTimestamp(ts);

        timeEventService.createEvent(dto1);

        assertThrows(DataIntegrityViolationException.class, () -> timeEventService.createEvent(dto2));
    }

    @Test
    void testCreateEventAllowsNullTimestampFailsDueToNotNullConstraint() {
        TimeEventDTO dto = new TimeEventDTO();
        dto.setTimestamp(null);

        assertThrows(DataIntegrityViolationException.class, () -> timeEventService.createEvent(dto));
    }
}