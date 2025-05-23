package id.ac.ui.cs.advprog.sistemticket.event;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;

public class TicketPurchasedEvent {
    private final Ticket ticket;
    private final int amount;
    
    public TicketPurchasedEvent(Ticket ticket, int amount) {
        this.ticket = ticket;
        this.amount = amount;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public int getAmount() {
        return amount;
    }
}
