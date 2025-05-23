package id.ac.ui.cs.advprog.sistemticket.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TicketEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TicketEventListener.class);
    
    @Async
    @EventListener
    public void handleTicketPurchasedEvent(TicketPurchasedEvent event) {
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
    }
}
