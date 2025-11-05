package com.leon.timerest.controller;

import com.leon.timerest.model.TimeEventEntity;
import com.leon.timerest.service.TimeEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class TimeEventControllerTest {

	@Mock
	private TimeEventService timeEventService;

	@InjectMocks
	private TimeEventController controller;

	private TimeEventEntity ev1;
	private TimeEventEntity ev2;
	private TimeEventEntity ev3;

	@BeforeEach
	void setUp() {
		ev1 = new TimeEventEntity();
		ev1.setId(1L);
		ev1.setTimestamp(LocalDateTime.of(2025, 11, 1, 10, 0));

		ev2 = new TimeEventEntity();
		ev2.setId(2L);
		ev2.setTimestamp(LocalDateTime.of(2025, 11, 1, 10, 1));

		ev3 = new TimeEventEntity();
		ev3.setId(3L);
		ev3.setTimestamp(LocalDateTime.of(2025, 11, 1, 10, 2));
	}

	@Test
	void ticksReturnsOkWithPageContent() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<TimeEventEntity> page = new PageImpl<>(List.of(ev1, ev2, ev3), pageable, 3);
		when(timeEventService.findAll(pageable)).thenReturn(page);

		ResponseEntity<Page<TimeEventEntity>> response = controller.ticks(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(3, response.getBody().getTotalElements());
		assertEquals(3, response.getBody().getContent().size());
		verify(timeEventService, times(1)).findAll(pageable);
	}

	@Test
	void ticksRespectsCustomPageableAndPassesThrough() {
		Pageable pageable = PageRequest.of(1, 10);
		Page<TimeEventEntity> page = new PageImpl<>(List.of(ev1), pageable, 11);
		when(timeEventService.findAll(pageable)).thenReturn(page);

		ResponseEntity<Page<TimeEventEntity>> response = controller.ticks(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().getContent().size());
		assertEquals(11, response.getBody().getTotalElements());
		assertEquals(1, response.getBody().getNumber());
		assertEquals(10, response.getBody().getSize());
	}

	@Test
	void ticksReturnsEmptyPage() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<TimeEventEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
		when(timeEventService.findAll(pageable)).thenReturn(emptyPage);

		ResponseEntity<Page<TimeEventEntity>> response = controller.ticks(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().getContent().isEmpty());
		assertEquals(0, response.getBody().getTotalElements());
	}
}
