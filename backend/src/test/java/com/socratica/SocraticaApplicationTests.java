package com.socratica;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "socratica.gemini.api-key=test-api-key",
        "spring.data.mongodb.uri=mongodb://localhost:27017/socratica_test"
})
class SocraticaApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }
}
