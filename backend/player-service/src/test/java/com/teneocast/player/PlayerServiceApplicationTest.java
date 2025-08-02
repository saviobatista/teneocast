package com.teneocast.player;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PlayerServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // with all the beans configured correctly
    }
}