package id.ac.ui.cs.advprog.sistemticket.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AsyncConfigTest {

    @Autowired
    private AsyncConfig asyncConfig;

    @Test
    void testTaskExecutorConfiguration() {
        // Get executor from the configuration
        Executor executor = asyncConfig.taskExecutor();
        
        // Verify executor is of the expected type
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
        
        // Cast to access properties
        ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
        
        // Verify properties match configuration
        assertEquals(2, taskExecutor.getCorePoolSize());
        assertEquals(5, taskExecutor.getMaxPoolSize());
        assertEquals(100, taskExecutor.getQueueCapacity());
        assertTrue(taskExecutor.getThreadNamePrefix().contains("TicketAsync-"));
    }
}
