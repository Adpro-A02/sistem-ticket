package id.ac.ui.cs.advprog.sistemticket.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class TicketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketEventListener.class);
    
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    
    @Async
    @EventListener
    @Timed(value = "ticket.event.processing.time", description = "Time taken to process ticket events")
    public void handleTicketPurchasedEvent(TicketPurchasedEvent event) {
        Timer.Sample sample = null;
        if (meterRegistry != null) {
            sample = Timer.start(meterRegistry);
        }
        
        try {
            // This method runs asynchronously when a ticket is purchased
            logger.info("Ticket purchased event received: {} tickets of type {} for event {}",
                       event.getAmount(),
                       event.getTicket().getType(),
                       event.getTicket().getEventId());
            
            // Here you could:
            // 1. Send email notifications to the buyer
            // 2. Update purchase statistics
            // 3. Notify event organizers
            // 4. Generate PDF tickets
            
            // Record the event processing in a counter
            if (meterRegistry != null) {
                meterRegistry.counter("ticket.events.processed", 
                        "eventType", "purchase", 
                        "ticketType", event.getTicket().getType())
                        .increment();
            }
        } finally {
            // Record how long it took to process the event
            if (sample != null && meterRegistry != null) {
                sample.stop(meterRegistry.timer("ticket.event.processing.time", 
                        "eventType", "purchase",
                        "ticketType", event.getTicket().getType()));
            }
        }
    }
}
