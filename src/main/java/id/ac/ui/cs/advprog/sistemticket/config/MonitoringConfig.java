package id.ac.ui.cs.advprog.sistemticket.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitoringConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    // Counters for ticket operations
    @Bean
    public Counter ticketPurchasedCounter(MeterRegistry registry) {
        return Counter.builder("ticket.purchased.count")
                .description("Number of tickets purchased")
                .register(registry);
    }
    
    @Bean
    public Timer ticketPurchaseTimer(MeterRegistry registry) {
        return Timer.builder("ticket.purchase.time")
                .description("Time taken to purchase tickets")
                .register(registry);
    }
    
    @Bean
    public Counter ticketCreatedCounter(MeterRegistry registry) {
        return Counter.builder("ticket.created.count")
                .description("Number of tickets created")
                .register(registry);
    }
    
    @Bean
    public Counter ticketStatusUpdateCounter(MeterRegistry registry) {
        return Counter.builder("ticket.status.updated.count")
                .description("Number of ticket status updates")
                .register(registry);
    }
}
