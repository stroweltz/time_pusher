package com.leon.timeconsumer;

import org.springframework.boot.SpringApplication;

public class TestTimeConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.from(TimeConsumerApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
