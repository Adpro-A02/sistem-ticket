package id.ac.ui.cs.advprog.sistemticket.scheduler;

import id.ac.ui.cs.advprog.sistemticket.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TicketExpirationScheduler {

    private final TicketService ticketService;

    @Autowired
    public TicketExpirationScheduler(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Scheduled(fixedRate = 3600000) // Run every hour
    public void checkExpiredTickets() {
        ticketService.checkAndUpdateExpiredTickets();
    }
}