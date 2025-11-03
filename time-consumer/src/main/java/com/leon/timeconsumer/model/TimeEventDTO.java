package com.leon.timeconsumer.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TimeEventDTO {
    private LocalDateTime timestamp;
}
