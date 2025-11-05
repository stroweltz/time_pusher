package com.leon.timeproducer.producer;

import com.leon.timeproducer.model.TimeEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimeEventScheduler {

    private final TimeEventProducer timeEventProducer;

    @Scheduled(fixedRateString = "${spring.scheduler.producer.fixed-rate-ms:1000}", initialDelay = 10000)
    public void produceTimeEvent() {
        LocalDateTime nowRounded = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        TimeEventDTO event = new TimeEventDTO();
        event.setTimestamp(nowRounded);
        timeEventProducer.sendTimeEvent(event);
        log.debug("Produced time event at {}", nowRounded);
    }
}


