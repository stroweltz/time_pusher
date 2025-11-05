package com.leon.timeproducer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TimeProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeProducerApplication.class, args);
    }

}
