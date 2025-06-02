package id.ac.ui.cs.advprog.sistemticket.event;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import id.ac.ui.cs.advprog.sistemticket.enums.TicketStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class TicketEventListenerTest {

    @InjectMocks
    private TicketEventListener ticketEventListener;
    
    @Test
    void testHandleTicketPurchasedEvent() {
        // Create a sample ticket
        Ticket ticket = new Ticket();
        ticket.setId("test-id");
        ticket.setEventId("event-id");
        ticket.setType("REGULAR");
        ticket.setPrice(150.0);
        ticket.setQuota(100);
        ticket.setRemainingQuota(95);
        ticket.setStatus(TicketStatus.AVAILABLE.getValue());
        
        // Create the event
        TicketPurchasedEvent event = new TicketPurchasedEvent(ticket, 5);
        
        // Verify event handler doesn't throw exceptions (works without metrics)
        assertDoesNotThrow(() -> ticketEventListener.handleTicketPurchasedEvent(event));
    }
}
