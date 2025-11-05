package com.leon.timerest.controller;

import com.leon.timerest.model.TimeEventEntity;
import com.leon.timerest.repository.TimeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.leon.timerest.TestcontainersConfiguration;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@Testcontainers
@ActiveProfiles("test")
class TimeEventControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TimeEventRepository timeEventRepository;

    @BeforeEach
    void setUp() {
        timeEventRepository.deleteAll();
    }

    @Test
    void testTicksReturnsEmptyPageWhenNoEvents() {
        ResponseEntity<RestResponsePage<TimeEventEntity>> response = restTemplate.exchange(
                "http://localhost:" + port + "/time-events",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<TimeEventEntity>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());
        assertEquals(0, response.getBody().getTotalElements());
    }

    @Test
    void testTicksReturnsPageWithEvents() {
        TimeEventEntity event1 = new TimeEventEntity();
        event1.setTimestamp(LocalDateTime.now().minusMinutes(10));
        timeEventRepository.save(event1);

        TimeEventEntity event2 = new TimeEventEntity();
        event2.setTimestamp(LocalDateTime.now().minusMinutes(5));
        timeEventRepository.save(event2);

        TimeEventEntity event3 = new TimeEventEntity();
        event3.setTimestamp(LocalDateTime.now());
        timeEventRepository.save(event3);

        ResponseEntity<RestResponsePage<TimeEventEntity>> response = restTemplate.exchange(
                "http://localhost:" + port + "/time-events",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<TimeEventEntity>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getContent().size());
        assertEquals(3, response.getBody().getTotalElements());
    }

    @Test
    void testTicksWithPagination() {
        for (int i = 0; i < 25; i++) {
            TimeEventEntity event = new TimeEventEntity();
            event.setTimestamp(LocalDateTime.now().minusMinutes(25 - i));
            timeEventRepository.save(event);
        }

        ResponseEntity<RestResponsePage<TimeEventEntity>> response1 = restTemplate.exchange(
                "http://localhost:" + port + "/time-events?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<TimeEventEntity>>() {}
        );

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertNotNull(response1.getBody());
        assertEquals(10, response1.getBody().getContent().size());
        assertEquals(25, response1.getBody().getTotalElements());
        assertEquals(0, response1.getBody().getNumber());

        ResponseEntity<RestResponsePage<TimeEventEntity>> response2 = restTemplate.exchange(
                "http://localhost:" + port + "/time-events?page=1&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<TimeEventEntity>>() {}
        );

        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertNotNull(response2.getBody());
        assertEquals(10, response2.getBody().getContent().size());
        assertEquals(25, response2.getBody().getTotalElements());
        assertEquals(1, response2.getBody().getNumber());
    }

    @Test
    void testTicksWithDefaultPageSize() {
        for (int i = 0; i < 5; i++) {
            TimeEventEntity event = new TimeEventEntity();
            event.setTimestamp(LocalDateTime.now().minusMinutes(5 - i));
            timeEventRepository.save(event);
        }

        ResponseEntity<RestResponsePage<TimeEventEntity>> response = restTemplate.exchange(
                "http://localhost:" + port + "/time-events",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<RestResponsePage<TimeEventEntity>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getContent().size());
        assertEquals(20, response.getBody().getSize()); // Default size
    }
}

