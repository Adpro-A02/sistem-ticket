package id.ac.ui.cs.advprog.sistemticket.service;

import id.ac.ui.cs.advprog.sistemticket.model.Ticket;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TicketService {
    public Ticket createTicket(Ticket ticket);
    public Ticket findById(String id);
    public List<Ticket> findAll();
    public List<Ticket> findAllByEventId(String eventId);
    public List<Ticket> findAllByType(String type);
    public List<Ticket> findAllAvailable(Long currentTime);
    public Ticket updateTicket(Ticket ticket);
    public Ticket updateStatus(String id, String status);
    public Ticket purchaseTicket(String id, int amount, Long currentTime);
    public void deleteTicket(String id);
    public CompletableFuture<Void> processTicketExpiration(String id);
}
