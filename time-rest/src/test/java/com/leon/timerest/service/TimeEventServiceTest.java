package com.leon.timerest.service;

import com.leon.timerest.model.TimeEventEntity;
import com.leon.timerest.repository.TimeEventRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeEventServiceTest {

	@Mock
	private TimeEventRepository timeEventRepository;

	@InjectMocks
	private TimeEventService timeEventService;

	private TimeEventEntity ev1;
	private TimeEventEntity ev2;
	private TimeEventEntity ev3;

	@BeforeEach
	void setUp() {
		ev1 = new TimeEventEntity();
		ev1.setId(1L);
		ev1.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));

		ev2 = new TimeEventEntity();
		ev2.setId(2L);
		ev2.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 1));

		ev3 = new TimeEventEntity();
		ev3.setId(3L);
		ev3.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 2));
	}

	@Test
	void findAllreturnsPageFromRepositorydefaultPageable() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<TimeEventEntity> repoPage = new PageImpl<>(List.of(ev1, ev2, ev3), pageable, 3);
		when(timeEventRepository.findAll(pageable)).thenReturn(repoPage);

		Page<TimeEventEntity> result = timeEventService.findAll(pageable);

		assertNotNull(result);
		assertEquals(3, result.getContent().size());
		assertEquals(3, result.getTotalElements());
		assertEquals(0, result.getNumber());
		assertEquals(20, result.getSize());
		verify(timeEventRepository, times(1)).findAll(pageable);
	}

	@Test
	void findAllrespectsCustomPageable_andPropagatesTotals() {
		Pageable pageable = PageRequest.of(1, 10);
		Page<TimeEventEntity> repoPage = new PageImpl<>(List.of(ev1, ev2), pageable, 12);
		when(timeEventRepository.findAll(pageable)).thenReturn(repoPage);

		Page<TimeEventEntity> result = timeEventService.findAll(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals(12, result.getTotalElements());
		assertEquals(1, result.getNumber());
		assertEquals(10, result.getSize());
		verify(timeEventRepository, times(1)).findAll(pageable);
	}

	@Test
	void findAllreturnsEmptyPage() {
		Pageable pageable = PageRequest.of(0, 5);
		Page<TimeEventEntity> repoPage = new PageImpl<>(List.of(), pageable, 0);
		when(timeEventRepository.findAll(pageable)).thenReturn(repoPage);

		Page<TimeEventEntity> result = timeEventService.findAll(pageable);

		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		assertEquals(0, result.getContent().size());
		verify(timeEventRepository, times(1)).findAll(pageable);
	}
}
