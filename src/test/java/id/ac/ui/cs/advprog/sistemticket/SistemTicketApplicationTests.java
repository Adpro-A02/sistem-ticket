package id.ac.ui.cs.advprog.sistemticket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "JWT_SECRET=test-jwt-secret-key-for-testing-purposes-must-be-long-enough",
    "spring.main.allow-bean-definition-overriding=true"
})
class SistemTicketApplicationTests {

    @Test
    void contextLoads() {
    }

}
