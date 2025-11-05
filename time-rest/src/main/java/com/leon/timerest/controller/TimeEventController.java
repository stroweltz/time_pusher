package com.leon.timerest.controller;

import com.leon.timerest.model.TimeEventEntity;
import com.leon.timerest.service.TimeEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class TimeEventController {

    private final TimeEventService timeEventService;

    @GetMapping("/time-events")
    public ResponseEntity<Page<TimeEventEntity>> ticks(
            @PageableDefault(size = 20) Pageable pageable) {

            var body = timeEventService.findAll(pageable);
            return ResponseEntity.ok(body);
    }


}
