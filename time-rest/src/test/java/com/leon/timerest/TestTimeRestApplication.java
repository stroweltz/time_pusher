package com.leon.timerest;

import org.springframework.boot.SpringApplication;

public class TestTimeRestApplication {

    public static void main(String[] args) {
        SpringApplication.from(TimeRestApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
