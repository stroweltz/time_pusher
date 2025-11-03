package com.leon.timerest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TimeRestApplicationTests {

    @Test
    void contextLoads() {
    }

}
